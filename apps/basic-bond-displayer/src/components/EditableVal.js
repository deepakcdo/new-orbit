import React, { useState } from "react";
import axios from "axios";
import { Alert, TreeItem } from "@material-ui/lab";
import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Snackbar,
} from "@material-ui/core";
import "./Entry.css";

function EditableVal(props) {
  const [newVal, setNewVal] = useState(props.parentObj[props.valKey]);
  const changeVal = (event) => {
    setNewVal(event.target.value);
  };

  // controls success/failure snackbar
  const [failed, setFailed] = useState(false);
  const closeFailed = () => {
    setFailed(false);
  };
  const [success, setSuccess] = useState(false);
  const closeSuccess = () => {
    setSuccess(false);
  };
  const [errorMessage, setErrorMessage] = useState("Unexpected error");

  // controls the popup dialog for changing the value
  const [dialogOpen, setDialogOpen] = useState(false);
  const handleClickOpen = () => {
    setDialogOpen(true);
  };
  const handleClose = () => {
    setDialogOpen(false);
  };
  const handleSave = () => {
    axios
      .put(
        "http://instrument-server-new-orbit-helen.e4ff.pro-eu-west-1.openshiftapps.com/api/put",
        {
          isin: props.isin,
          value: newVal,
          path: props.path.slice(1),
        }
      )
      .then(() => {
        props.parentObj[props.valKey] = newVal;
        // sends the result down to the root so everthing is consistent
        props.commitEdit();
        setSuccess(true);
      })
      .catch((error) => {
        if (error.response.status === 401) {
          setErrorMessage(error.response.data.message);
        } else {
          setErrorMessage("Unexpected error");
        }
        setFailed(true);
      });
    setDialogOpen(false);
  };

  let contents = (
    <>
      {props.valKey}{" "}
      <span className="BoldText">{props.parentObj[props.valKey]}</span>
    </>
  );

  return (
    <>
      <TreeItem
        onClick={handleClickOpen}
        endIcon={props.endIcon}
        label={contents}
      ></TreeItem>
      <Dialog open={dialogOpen} onClose={handleClose}>
        <DialogTitle id="bond-change-dialog">Edit {props.valKey}</DialogTitle>
        <DialogContent>
          <TextField
            id="newVal"
            name="newVal"
            label="New Value"
            value={newVal}
            onChange={changeVal}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancel</Button>
          <Button onClick={handleSave}>Save</Button>
        </DialogActions>
      </Dialog>
      <Snackbar open={failed} autoHideDuration={3000} onClose={closeFailed}>
        <Alert onClose={closeFailed} severity="error">
          {errorMessage}
        </Alert>
      </Snackbar>
      <Snackbar open={success} autoHideDuration={3000} onClose={closeSuccess}>
        <Alert onClose={closeSuccess} severity="success">
          Bond updated!
        </Alert>
      </Snackbar>
    </>
  );
}

export default EditableVal;
