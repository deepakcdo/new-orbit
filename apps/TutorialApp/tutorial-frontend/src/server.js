import axios from "axios";

var server_ip_address = process.env.REACT_APP_COMPONENT_BACKEND_HOST || 'localhost';
var server_port = process.env.REACT_APP_COMPONENT_BACKEND_PORT || 8080;

export default axios.create({
    baseURL: "http://" + server_ip_address + ":" + server_port +  "/api",
    headers: {
        "Content-type": "application/json"
    }
});
