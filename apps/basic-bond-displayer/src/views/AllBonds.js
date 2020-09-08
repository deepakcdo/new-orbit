import React, { useState, useEffect } from "react";
import axios from "axios";
import { Grid, IconButton } from "@material-ui/core";
import { ChevronLeft, ChevronRight } from "@material-ui/icons";
import Entry from "../components/Entry";
import Search from "../components/Search";
import "./Bonds.css";

function AllBonds(props) {
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

  const [bonds, setBonds] = useState(
    "Please select number of bonds you want to load"
  );
  const getAllBonds = () => {
    // request with variable parameters (including size and startind index)
    let query = "";
    for (let param in filters) {
      query += "&" + param.toString() + "=" + filters[param].toString();
    }
    axios
      .get(
        "http://instrument-server-new-orbit-helen.e4ff.pro-eu-west-1.openshiftapps.com/api/filter?startIndex=" +
          (page * filters.pageSize).toString() +
          "&endIndex=" +
          ((page + 1) * filters.pageSize - 1).toString() +
          query
      )
      .then((res) => {
        // turns json array into array of components
        setBonds(
          res.data.map((entry, index) => (
            <Grid key={entry.isin} item xs={12} md={6} lg={4}>
              <Entry
                key={entry.isin}
                bondData={entry}
                categories={categories}
                index={index}
              />
            </Grid>
          ))
        );
      })
      .catch(() => {
        setBonds("Error: bonds could not be retrieved");
      });
  };

  // setfilters is sent to search component to modify
  const [filters, setFilters] = useState({
    longDes: "",
    initialISIN: "",
    pageSize: 15,
  });

  // keeps track of which page of results the user is on
  const [page, setPage] = useState(0);
  const nextPage = () => {
    if (bonds.length > 0) {
      setPage(page + 1);
    }
  };
  const prevPage = () => {
    if (page > 0) {
      setPage(page - 1);
    }
  };

  // gets bonds every time the page changes
  useEffect(() => {
    getAllBonds();
  }, [page]);

  return (
    <>
      <Grid container>
        <Grid item xs={6} className="LeftHeader">
          <span className="CenterVertical">
            <Search
              filters={filters}
              change={setFilters}
              getBonds={getAllBonds}
            />
          </span>
        </Grid>
        <Grid item xs={6} className="RightHeader">
          {page > 0 && (
            <IconButton color="primary" onClick={prevPage}>
              <ChevronLeft />
            </IconButton>
          )}
          <p color="primary">Page {page + 1}</p>
          {bonds.length > 0 && (
            <IconButton color="primary" onClick={nextPage}>
              <ChevronRight />
            </IconButton>
          )}
        </Grid>
        {
          // the actual bonds which get displayed is there are any
        }
        {bonds.length > 0 ? bonds : "End of search results"}
      </Grid>
    </>
  );
}

export default AllBonds;
