import React, { useState } from "react";
import axios from "axios";
import { Button, TextField } from "@material-ui/core";
import Bond from "../components/Bond";
import "./Bonds.css";

function SingleBond(props) {
  // placeholder categorization
  const categories = {
    economic: new Set([
      "currency",
      "maturityType",
      "longDescription",
      "calculationType",
      "maturityDate",
      "earliestMaturityDate",
      "marketSector",
    ]),
    credit: new Set([
      "freq",
      "isFloat",
      "firstConformingDate",
      "lastConformingDate",
    ]),
    trading: new Set([
      "minAmount",
      "minPiece",
      "minIncrement",
      "settlementDays",
    ]),
  };

  // keeps track of the isin number of bond being currently displayed
  const [bondID, setBondID] = useState("AAAAAAAAAAAA");
  const handleBondChange = (event) => {
    setBondID(event.target.value);
  };

  // holds the data to the bond currently displayed
  const [bondData, setBondData] = useState({
    please: {
      change: "me",
    },
  });
  // fetches a bond based on isin from instrument server
  const updateBond = () => {
    axios
      .get(
        "http://instrument-server-new-orbit-helen.e4ff.pro-eu-west-1.openshiftapps.com/api/" +
          bondID
      )
      .then((res) => {
        setBondData(res.data);
      })
      .catch(() => {
        setBondData({ id: "invalid" });
      });
  };

  return (
    <>
      <div className="LeftHeader">
        <TextField
          autoFocus
          id="bond-id"
          label="Bond ID"
          value={bondID}
          onChange={handleBondChange}
          InputLabelProps={{
            shrink: true,
          }}
          variant="outlined"
        />
        <span className="HorizontalSpace CenterVertical">
          <Button variant="contained" color="primary" onClick={updateBond}>
            Change bond
          </Button>
        </span>
      </div>
      <Bond bond={bondData} categories={categories} />
    </>
  );
}

export default SingleBond;
