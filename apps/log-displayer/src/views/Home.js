import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Grid,
  CircularProgress,
  LinearProgress,
  TextField,
  Button,
} from "@material-ui/core";
import {
  FlexibleWidthXYPlot,
  YAxis,
  AreaSeries,
  GradientDefs,
} from "react-vis";
import LogEntry from "../components/LogEntry";
import "./Home.css";

function Home() {
  // cards of logs displayed to user
  const [cards, setCards] = useState(<p>loading...</p>);
  // number of cards on the screen at one time
  const [numCards, setNumCards] = useState(0);
  const expandCards = () => {
    if (numCards < numLogs) {
      let newNum = Math.min(numCards + 48, numLogs);
      setCards(logs.slice(0, newNum));
      setNumCards(newNum);
    }
  };
  // controls percentile bars
  const [pc, setPc] = useState([0, 0, 0, 0]);
  // all the card components, but not necessarily shown
  const [logs, setLogs] = useState([]);
  const [graph, setgraph] = useState(<p>loading...</p>);
  const [succeeded, setSucceeded] = useState(0);
  // number of logs, controls graph and max cards possible
  const [numLogs, setNumLogs] = useState(200);
  // error dsplayed if user enters invalid integer
  const [logNumError, setLogNumError] = useState("");
  const changeLogNum = (event) => {
    if (event.target.value < 1) {
      setLogNumError("Please enter a positive number");
    } else {
      setNumLogs(event.target.value);
      if (logNumError.length > 0) {
        setLogNumError("");
      }
    }
  };

  // converts the api time format to number of seconds
  const calToMS = (s) => {
    let times = s.split(":");
    let ans = times[2];
    ans += times[1] * 60;
    ans += times[0] * 3600;
    if (isNaN(ans)) {
      console.log("Failed to convert this to a time: " + s);
      return 0;
    } else {
      return ans;
    }
  };

  useEffect(() => {
    axios
      .get(
        "http://log-server-new-orbit-mykola.e4ff.pro-eu-west-1.openshiftapps.com/api/0/" +
          numLogs.toString()
      )
      .then((res) => {
        let calDurations = [];
        let failedNum = 0;
        let graphI = { data: [] };
        let i = 0;
        for (let log of res.data) {
          if (log.response_sent) {
            calDurations.push(calToMS(log.cal_duration));
            graphI.data.push({
              x: numLogs - i,
              y: calToMS(log.cal_duration),
              y0: 0,
            });
            i++;
          } else {
            failedNum++;
          }
        }

        setgraph(
          <FlexibleWidthXYPlot colorType="linear" height={350}>
            <GradientDefs>
              <linearGradient id="GraphGradient" x1="0" x2="0" y1="0" y2="1">
                <stop offset="0%" stopColor="red" stopOpacity={0.8} />
                <stop offset="100%" stopColor="blue" stopOpacity={0.3} />
              </linearGradient>
            </GradientDefs>
            <YAxis />
            <AreaSeries color={"url(#GraphGradient)"} {...graphI} />
          </FlexibleWidthXYPlot>
        );

        calDurations.sort();
        if (calDurations.length > 0) {
          var pc50 = Math.floor(calDurations.length / 2);
          var pc90 = Math.floor((calDurations.length * 3) / 4);
          var pc95 = Math.floor((calDurations.length * 19) / 20);
          var pc99 = Math.floor((calDurations.length * 99) / 100);
          setPc([
            calDurations[pc50],
            calDurations[pc90],
            calDurations[pc95],
            calDurations[pc99],
          ]);
        }
        setSucceeded((res.data.length - failedNum) / res.data.length);

        // all logs
        setLogs(
          res.data.map((calc, index) => (
            <Grid item xs={3} lg={2} key={index}>
              <LogEntry {...calc} />
            </Grid>
          ))
        );
        // only the first 12 initially
        setCards(
          res.data.slice(0, 12).map((calc, index) => (
            <Grid item xs={3} lg={2} key={index}>
              <LogEntry {...calc} />
            </Grid>
          ))
        );
        setNumCards(12);
      })
      .catch((error) => {
        console.log(error);
      });
  }, [numLogs]);

  return (
    <Grid container className="Panel">
      <Grid item xs={12}>
        {graph}
      </Grid>
      <Grid item xs={12} md={6} className="CenterParent">
        <LinearProgress
          variant="determinate"
          value={(pc[0] / pc[3]) * 100}
          className="CenterChild"
        />
        <div className="CenterChild">
          <span>Median</span>
          <span className="BoldText"> {pc[0]} s</span>
        </div>
        <LinearProgress
          variant="determinate"
          value={(pc[1] / pc[3]) * 100}
          className="CenterChild"
        />
        <div className="CenterChild">
          <span>Bottom 25%</span>
          <span className="BoldText"> {pc[1]} s</span>
        </div>
        <LinearProgress
          variant="determinate"
          value={(pc[2] / pc[3]) * 100}
          className="CenterChild"
        />
        <div className="CenterChild">
          <span>Bottom 5%</span>
          <span className="BoldText"> {pc[2]} s</span>
        </div>{" "}
        <LinearProgress
          variant="determinate"
          value={100}
          className="CenterChild"
        />
        <div className="CenterChild">
          <span>Bottom 1%</span>
          <span className="BoldText"> {pc[3]} s</span>
        </div>{" "}
      </Grid>
      <Grid item xs={12} md={6} className="CenterParent">
        <CircularProgress variant="static" size={100} value={succeeded * 100} />
        <p className="CenterChild">
          <span className="BoldText">{(succeeded * 100).toFixed(2)}%</span> of
          the last <span className="BoldText">{logs.length}</span> calculations
          were successfully completed
        </p>
      </Grid>
      <Grid item xs={12} className="CenterParent">
        <div className="Spaced">
          <TextField
            id="logNum"
            name="logNum"
            label="Number of Logs to display"
            type="number"
            defaultValue={200}
            onChange={changeLogNum}
            variant="outlined"
            error={logNumError.length !== 0}
            helperText={logNumError}
            className="CenterChild"
          />
        </div>
      </Grid>
      <Grid container key={numCards}>
        {cards}
      </Grid>
      {numCards < numLogs && ( // only visible if not all cards are shown
        <Grid item xs={12} className="CenterParent">
          <div className="Spaced">
            <Button
              size="large"
              variant="contained"
              color="primary"
              disableElevation
              onClick={expandCards}
            >
              See more
            </Button>
          </div>
        </Grid>
      )}
    </Grid>
  );
}

export default Home;
