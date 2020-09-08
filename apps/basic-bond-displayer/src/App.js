import React from "react";
import { Button, AppBar, Toolbar } from "@material-ui/core";
import { HashRouter as Router, Route, Link, Switch } from "react-router-dom";
import "../node_modules/react-vis/dist/style.css";
import AllBonds from "./views/AllBonds";
import SingleBond from "./views/SingleBond";
import "./App.css";

function App() {
  // The single bond page is relatively redundant, mostly
  // useful for testing if an error is caused by trying to
  // render all the bonds at once - often caused by key issues
  return (
    <Router>
      <AppBar position="static" color="primary">
        <Toolbar>
          <Button>
            <Link to="/all" className="App-nav">
              All bonds
            </Link>
          </Button>
          <Button>
            <Link to="/bond" className="App-nav">
              Single bond
            </Link>
          </Button>
        </Toolbar>
      </AppBar>

      <Switch>
        <Route path="/all">
          <AllBonds />
        </Route>
        <Route path="/bond">
          <SingleBond />
        </Route>
      </Switch>
    </Router>
  );
}

export default App;
