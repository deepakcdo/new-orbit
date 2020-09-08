import React, { useState } from "react";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
} from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import "./Search.css";

function Search(props) {
  // this is a little box at the bottom of the search pop up
  const [submissionAlert, setSubmissionAlert] = useState(<span></span>);

  // used for validating input
  const rules = {
    initialISIN: (inputISIN) => {
      return inputISIN.length === 2 || inputISIN.length === 0;
    },
    pageSize: (inputPageSize) => {
      return !isNaN(Number(inputPageSize)) && Number(inputPageSize) > 0;
    },
  };
  const [iSINError, setISINError] = useState("");
  const [pageSizeError, setpageSizeError] = useState("");
  const [filters, setFilters] = useState(props.filters);
  const changeFilter = (event) => {
    let newFilters = filters;
    newFilters[event.target.name] = event.target.value;
    if (rules.hasOwnProperty(event.target.name)) {
      if (rules[event.target.name](event.target.value)) {
        if (event.target.name === "initialISIN") {
          setISINError("");
        } else if (event.target.name === "pageSize") {
          setpageSizeError("");
        }
      } else {
        if (event.target.name === "initialISIN") {
          setISINError("Please enter 2 characters");
        } else if (event.target.name === "pageSize") {
          setpageSizeError("Please enter a positive number");
        }
      }
    }
    setFilters(newFilters);
  };

  // controls the popup dialog for changing settings
  const [dialogOpen, setDialogOpen] = useState(false);
  const handleClickOpen = () => {
    setDialogOpen(true);
  };
  const handleClose = () => {
    setDialogOpen(false);
  };
  const handleApply = () => {
    if (iSINError.length === 0 && pageSizeError.length === 0) {
      props.change(filters); // sends the filters data back to parent component
      props.getBonds();
      setSubmissionAlert(<span></span>);
      setDialogOpen(false);
    } else {
      setSubmissionAlert(
        <Alert className="FieldBlock" severity="error">
          Enter all fields correctly
        </Alert>
      );
    }
  };

  return (
    <>
      <Button variant="contained" color="primary" onClick={handleClickOpen}>
        Search
      </Button>
      <Dialog open={dialogOpen} onClose={handleClose}>
        <DialogTitle id="bond-change-dialog">Search and filters</DialogTitle>
        <DialogContent>
          <TextField
            id="longDes"
            name="longDes"
            label="Long Description"
            defaultValue={filters.longDes}
            onChange={changeFilter}
            variant="outlined"
          />
          <div className="FieldBlock">
            <TextField
              id="initialISIN"
              name="initialISIN"
              label="Match first 2 ISIN letters"
              defaultValue={filters.initialISIN}
              onChange={changeFilter}
              variant="outlined"
              error={iSINError.length !== 0}
              helperText={iSINError}
            />
          </div>
          <TextField
            id="pageSize"
            name="pageSize"
            label="Bonds per page"
            type="number"
            defaultValue={filters.pageSize}
            onChange={changeFilter}
            variant="outlined"
            error={pageSizeError.length !== 0}
            helperText={pageSizeError}
          />
          {submissionAlert}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleApply}>Apply</Button>
          <Button onClick={handleClose}>Close</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}

export default Search;
