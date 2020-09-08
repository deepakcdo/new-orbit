package main

import (
		"fmt"
		"os"
    "log"
    "net/http"
    "context"
    "encoding/json"
		"strconv"

    "go.mongodb.org/mongo-driver/mongo"
    "go.mongodb.org/mongo-driver/mongo/options"
    "go.mongodb.org/mongo-driver/bson"
    "go.mongodb.org/mongo-driver/bson/primitive"
    "github.com/gorilla/mux"
    "github.com/gorilla/handlers"
)

// DB - setting a global DB struct to be accessible to route functions
type DB struct {
	collection *mongo.Collection
}

// Calculation - the fields the json structure has
type Calculation struct {
	ID primitive.ObjectID `json:"_id,omitempty" bson:"_id,omitempty"`
	CalType int `bson:"cal_type" json:"cal_type"`
	RequestTime string `bson:"request_time" json:"request_time"`
	ResponseSent bool `bson:"response_sent" json:"response_sent"`
	ResponseTime string `bson:"response_time" json:"response_time"`
	Duration string `bson:"cal_duration" json:"cal_duration"`
	Num1 int `bson:"num1" json:"num1"`
	Num2 int `bson:"num2" json:"num2"`
	Response int `bson:"response" json:"response"`
}

// Define the routes
func main() {
	fmt.Printf("REST API using golang, connecting to Mongo...\n")

	// Set client options
	clientOptions := options.Client().ApplyURI(os.Getenv("mongoURI"))

	// Connect to MongoDB
	client, err := mongo.Connect(context.TODO(), clientOptions)
	if err != nil {
			log.Fatal(err)
	}

	// Check the connection
	err = client.Ping(context.TODO(), nil)

	if err != nil {
			log.Fatal(err)
	}
	defer client.Disconnect(context.TODO())

	// link up to relevant DB and collection
	collection := client.Database("audit").Collection("requests")
	db := &DB{collection: collection}

	fmt.Println("Connected to MongoDB!")

	//outputs
	fmt.Printf("Server listing")
	fmt.Printf("\nCTRL C to exit\n")

	// Controller for endpoints
	r := mux.NewRouter()

	// this is to allow quick setup...maybe not the best practice in production
	header := handlers.AllowedHeaders([]string{"X-Requested-With", "Content-Type", "Authorization"})
	methods := handlers.AllowedMethods([]string{"GET", "POST", "PUT", "HEAD", "OPTIONS"})
	origins := handlers.AllowedOrigins([]string{"*"})

	// routes
	api := r.PathPrefix("/api").Subrouter()
	api.HandleFunc("/", db.All).Methods("GET", "OPTIONS")
	api.HandleFunc("/{start}/{end}", db.Page).Methods("GET", "OPTIONS")
	r.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		_, _ = fmt.Fprint(w, "hey there, you might want to add /api to the url...")
	})

	if err := http.ListenAndServe(":8080", handlers.CORS(header, methods, origins)(r)); err != nil {
			log.Fatal(err)
	}
}

// All - respond with all calculations
func (db *DB)All(res http.ResponseWriter, req *http.Request){
	fmt.Println("All GET")

	// Sort in reverse order of them added (I think)
	findOptions := options.Find()
	findOptions.SetSort(bson.D{{Key: "$natural", Value: -1}})

	// create an array of data
	var results []Calculation
	var calc Calculation
	// set the api header
	res.Header().Set("content-type", "application/json")
	// use the find command to get all
	result, err := db.collection.Find(context.TODO(), bson.D{{}}, findOptions)
	if err != nil {
			fmt.Println("All GET failed to query DB", err)
	}

	// go through the result and decode each element one at a time
	for result.Next(context.TODO()){
			err := result.Decode(&calc)
			if err != nil {
					log.Println(err)
					// log.Fatal(err)
			} else {
					// add to the array
					results = append(results, calc)
			}
	}
	// return the array as json
	json.NewEncoder(res).Encode(results)
}

// Page - send a page worth of calculations
func (db *DB)Page(res http.ResponseWriter, req *http.Request){
	fmt.Println("Page GET")

	params := mux.Vars(req)
	start, err := strconv.Atoi(params["start"])
	if err != nil {
		fmt.Println("Could not parse start", err)
		start = 0
	}
	end, err := strconv.Atoi(params["end"])
	if err != nil {
		fmt.Println("Could not parse end", err)
		end = 0
	}

	// Sort in reverse order of them added (I think)
	findOptions := options.Find()
	findOptions.SetSort(bson.D{{Key: "$natural", Value: -1}})

	// create an array of data
	var results []Calculation
	var calc Calculation
	// set the api header
	res.Header().Set("content-type", "application/json")
	// use the find command to get all
	result, err := db.collection.Find(context.TODO(), bson.D{{}}, findOptions)
	if err != nil {
			fmt.Println("Page GET failed to query DB", err)
	}

	// go through the result and decode each element one at a time
	var i int = 0
	for i < end && result.Next(context.TODO()){
			err := result.Decode(&calc)
			if err != nil {
					log.Println(err)
					// log.Fatal(err)
			} else {
					// add to the array
					results = append(results, calc)
					i++
			}
	}
	// limit end so it does not go out of bounds (i = min(length of result, end))
	end = i
	// limit start so we are sure to get a valid slice
	if start > end {
		start = end
	}

	// return the array as json
	json.NewEncoder(res).Encode(results[start:end])
}
