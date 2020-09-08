### Introduction 

This is the frontend of the full stack demo project.

It is a React Javascript application to interact with the user.

For running on your local machine, change the line *"build": "react-scripts build"* in package.json into *"build": "react-scripts build && rm -rf ../contact-server/src/main/resources/static && mv build ../contact-server/src/main/resources/static"*.
                                                                                                 
Then, run 'npm run build' to run the frontend. (run 'npm install' to install the node_modules if running for the first time)