main.go contains all the code

The Dockerfile is set up to use the mod functionality to reduce redundant package upgrading during the build process.

Make sure to run the relevant commands so that the mod packages are set up on your machine - https://medium.com/@adiach3nko/package-management-with-go-modules-the-pragmatic-guide-c831b4eaaf31 should help you there.

To compile and run the code you can use the `go run` command.
The Dockerfile contains examples of how commands can be used.

### The Code

The Code contains 4 main components - the model, the router, the 2 functions, all and page, for getting data.

The model is a structure made up of fields that specify the data type, bson and json. The bson defines the key that the field has in mongo, and json defines the key that will be used when the response is written into a json object. The name of the field in the structure should start with a capital letter if you want it to be written to the response - these fields are exported in golang terms.

The router is in the main function, where it connects to mongo using an environment variable as the URI. Then it connects to the correct database and collection at that URI, and sets up headers, in this case to allow CORS to go through (but I guess you would have more fine grained setup if this was production). Beyond that is a simple router and subrouter, which delegates requests to the All and Page functions.

The all function works by setting the ordering to reverse of natural - which is the opposite of the order they were added to the database, since the most recent logs are appended to the "end" of the database. It decodes the entire collection, one at a time, and adds that to an array, which is the encoded into json and used in the response.

The page function works in a similar fashion, but it only decodes until the last index requested, and then encodes a slice of said array (the start index onward, not very efficient but I couldn't get mongo skip to work). It uses Atoi to decode the parameters in the url, the names and structure of which are defined in the router.

### Deployment

I used docker to build the image locally, then pushed it to dockerhub, and then subsequently used the deploy image functionality provided by openshift, set up a service and route and voila it works. I haven't figured out how it automatically deploy updates as I didn't spend an awfully long time on this.
