import React from "react";
import { Card, CardHeader, CardContent } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import "./LogEntry.css";

function LogEntry(props) {
  return (
    <Card className="Line">
      <CardHeader subheader={"Requested " + props.request_time} />
      <CardContent>
        <div>
          Calculation type
          <span className="BoldText"> {props.cal_type}</span>
        </div>
        <div>
          Input 1<span className="BoldText"> {props.num1}</span>
        </div>
        <div>
          Input 2<span className="BoldText"> {props.num2}</span>
        </div>
        {props.response_sent ? (
          <>
            <div>
              Response
              <span className="BoldText"> {props.response}</span>
            </div>
            <div>
              Response time
              <span className="BoldText"> {props.response_time}</span>
            </div>
            <div>
              Calculation duration
              <span className="BoldText"> {props.cal_duration}</span>
            </div>
            <Alert severity="success" variant="outlined" className="TopMargin">
              Fulfilled
            </Alert>
          </>
        ) : (
          <Alert severity="error" variant="outlined" className="TopMargin">
            Not fulfilled
          </Alert>
        )}
      </CardContent>
    </Card>
  );
}

export default LogEntry;
