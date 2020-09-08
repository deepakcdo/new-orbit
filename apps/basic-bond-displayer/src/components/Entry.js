import React, { useState, useEffect, useReducer } from "react";
import { XYPlot, YAxis, LineSeries, DiscreteColorLegend } from "react-vis";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Card,
  CardActions,
  CardContent,
  CardHeader,
} from "@material-ui/core";
import Bond from "./Bond";
import "./Entry.css";

function Entry(props) {
  const [prices, newPrices] = useReducer(updatePrices, {
    bids: [],
    offers: [],
    spread: undefined,
    bidGraph: {
      color: 0,
      data: [],
    },
    offerGraph: {
      color: 1,
      data: [],
    },
  });

  // signals update for graph
  const [graphKey, changeGraphKey] = useState(0);

  // title color according to spread
  const [titleColor, setTitleColor] = useState("GreenTitle");

  function updatePrices(oldPrices, update) {
    let updatedPrices = oldPrices;

    // update bids
    updatedPrices.bids.push(update.bid);
    if (updatedPrices.bids.length > 20) {
      updatedPrices.bids.shift();
    }
    let newBidsGraph = [];
    for (let i = 0; i < updatedPrices.bids.length; i++) {
      newBidsGraph.push({ x: i, y: updatedPrices.bids[i] });
    }
    updatedPrices.bidGraph.data = newBidsGraph;

    // update offers
    updatedPrices.offers.push(update.offer);
    if (updatedPrices.offers.length > 20) {
      updatedPrices.offers.shift();
    }
    let newOfferGraph = [];
    for (let i = 0; i < updatedPrices.offers.length; i++) {
      newOfferGraph.push({ x: i, y: updatedPrices.offers[i] });
    }
    updatedPrices.offerGraph.data = newOfferGraph;
    updatedPrices.spread = update.spread;

    return updatedPrices;
  }

  // data for legend
  const legends = [
    { title: "Bid", color: "teal" },
    { title: "Offer", color: "purple" },
  ];

  // sets up websocket connection for price graph
  useEffect(() => {
    let streamer = new WebSocket(
      "ws://instrument-enricher-new-orbit-helen.e4ff.pro-eu-west-1.openshiftapps.com/enriched-bonds/" +
        props.bondData.isin
    );
    // streamer.onopen = (event) => {};
    streamer.onmessage = (event) => {
      let update = JSON.parse(event.data);
      newPrices(update);

      // change title
      if (update.spread > 1000) {
        setTitleColor("RedTitle");
      } else if (update.spread > 750) {
        setTitleColor("");
      } else {
        setTitleColor("GreenTitle");
      }

      // signals the graph to re-render
      changeGraphKey(Math.random());
    };
    return () => {
      streamer.close();
    };
  }, []);

  // controls the popup dialog for viewing the bond
  const [dialogOpen, setDialogOpen] = useState(false);
  const handleClickOpen = () => {
    setDialogOpen(true);
  };
  const handleClose = () => {
    setDialogOpen(false);
  };

  return (
    <Card className="Card">
      <CardHeader title={props.bondData.isin} />
      <CardContent>
        <p>
          Isin <span className="BoldText">{props.bondData.isin} </span>
        </p>
        <p>
          Market sector{" "}
          <span className="BoldText">{props.bondData.marketSector} </span>
        </p>
        <p>
          Coupon currency{" "}
          <span className="BoldText">{props.bondData.couponCurrency} </span>
        </p>
        <p>
          Long description{" "}
          <span className="BoldText">{props.bondData.longDescription} </span>
        </p>
        <p>
          Spread{" "}
          <span className={"BoldText " + titleColor}>{prices.spread}</span>
        </p>
        <XYPlot
          colorType="linear"
          colorDomain={[0, 1]}
          colorRange={["teal", "purple"]}
          width={300}
          height={150}
          key={graphKey}
        >
          <YAxis />
          <LineSeries {...prices.offerGraph} />
          <LineSeries {...prices.bidGraph} />
        </XYPlot>
        <DiscreteColorLegend
          orientation="horizontal"
          width={300}
          items={legends}
        />
      </CardContent>
      <CardActions>
        <Button onClick={handleClickOpen}>View Bond</Button>
        <Dialog open={dialogOpen} onClose={handleClose}>
          <DialogTitle id="bond-change-dialog">
            Bond isin {props.bondData.isin}
          </DialogTitle>
          <DialogContent>
            <Bond bond={props.bondData} categories={props.categories} />
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClose}>Close</Button>
          </DialogActions>
        </Dialog>
      </CardActions>
    </Card>
  );
}

export default Entry;
