import React from "react";
import { ExpandMore, ChevronRight, Edit } from "@material-ui/icons";
import { TreeItem, TreeView } from "@material-ui/lab";
import EditableVal from "./EditableVal";

function Bond(props) {
  const bond = props.bond;
  const econ = props.categories.economic;
  const cred = props.categories.credit;
  // const trade = props.categories.trading;

  let economic = {};
  let credit = {};
  let trading = {};

  for (let key in bond) {
    if (econ.has(key)) {
      economic[key] = bond[key];
    } else if (cred.has(key)) {
      credit[key] = bond[key];
    } else {
      trading[key] = bond[key];
    }
  }

  let categorized = {
    Economic_data: economic,
    Credit_data: credit,
    Trading_data: trading,
  };

  // update surface level primitives
  const commitEdits = () => {
    for (let key in bond) {
      if (econ.has(key)) {
        bond[key] = economic[key];
      } else if (cred.has(key)) {
        bond[key] = credit[key];
      } else {
        bond[key] = trading[key];
      }
    }
  };

  const objectToTreeItem = (path, root, isin) => {
    // array stores path of nested object
    // isin is used so that all nodes are uniquely
    // identified (otherwise material ui can't
    // clean up as intended)
    if (typeof root !== "object") {
      // if a value passed in, it's an end node
      return root;
    } else {
      // for each object, wrap the value in an item
      // created using the key (and isin for unique id)
      let res = [];

      for (let objKey in root) {
        let newPath = path.slice();
        newPath.push(objKey);
        if (typeof root[objKey] !== "object") {
          let iconUsed;
          // currently hardcoded which values you can edit
          if (objKey === "longDescription" || objKey === "couponCurrency") {
            iconUsed = <Edit fontSize="small" />;
          } else {
            iconUsed = <Edit fontSize="small" color="disabled" />;
          }

          res.push(
            <EditableVal
              key={isin + objKey.toString()}
              valKey={objKey}
              parentObj={root}
              commitEdit={commitEdits}
              path={newPath}
              isin={isin}
              endIcon={iconUsed}
            />
          );
        } else {
          res.push(
            <TreeItem
              key={isin + objKey.toString()}
              nodeId={isin + objKey.toString()}
              label={objKey}
            >
              {objectToTreeItem(newPath, root[objKey], isin)}
            </TreeItem>
          );
        }
      }

      return res;
    }
  };

  return (
    <TreeView
      defaultCollapseIcon={<ExpandMore />}
      defaultExpanded={["root"]}
      defaultExpandIcon={<ChevronRight />}
      disableSelection={true}
    >
      {objectToTreeItem([], categorized, props.bond.isin)}
    </TreeView>
  );
}

export default Bond;
