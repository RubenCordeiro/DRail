# DRail

## What is it?

DRail is a railway management platform.
It is composed by two distinct mobile Android applications: one for ticket purchasers and another for ticket inspectors.

The Android application for ticket purchasers allows end users to consult information about daily timetables and ticket prices as well as buying tickets.

The Android application for ticket inspectors allows the end user to validate the emitted tickets for a specific trip.

## Architecture

The railway management platform follows client-server architecture composed by three layers: data, business logic and presentation layer.

The data layer consists of a Neo4j database instance. Neo4j is an open-source NoSQL graph database. 

The business logic layer consists of a Node.js server. It exposes a RESTful API and communicates with the Neo4j instance in order to retrieve and update all the information related to the railway platform.

The presentation layer is represented by the two mobile Android applications.
