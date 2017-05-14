# RapBot
Raspberry Pi Robot for autonomous land exploration.

## Introduction
This software guidebook provides an overview of the RapBot system. It includes a summary of the following:

1. The requirements, constraints and principles behind the project.
2. The software architecture, including the high-level technology choices and structure of the software.
3. The infrastructure architecture and how the software is deployed.
4. Operational and support aspects of the website.

## Context
RapBot is a distributed system used to control an autonmous land vehicle to explore indoor spaces autonomously. The system runs on a [Raspberry Pi 3 Model B](https://www.raspberrypi.org/products/raspberry-pi-3-model-b/). Using an [Adafruit Motor Hat](https://www.adafruit.com/product/2348) -- with stepper motors attached to a [robot kit](https://www.adafruit.com/product/2939) -- the RapBot will create a two-dimensional map of its surroundings. Users can then view a heat map of the space, where temperature is determined by the rate of change between runs of a specific point.

The purpose of RapBot is to provide a provide a proof-of-concept for indoor-space using autonmous vehicles.

__Placeholder for context diagram__

### Users
RapBot has a single type of user. Users have acess to start and stop the autonomous exploration, delete stored maps, and view a heat map. All functionality is provided to users through a web interface run on the Raspberry Pi when RapBot starts.

### External Systems
RapBot currently has no external dependencies.

## Functional Overview
This section provides a summary of the functionality provided by the RapBot system.

### Exploration
RapBot's primary purpose is autonomous exploration. For this purpose RapBot must scan its environment to detemrine its location within the space it is exploring. RapBot stores sensor data together with the two-dimensional map, so that we can test algorithmic changes on historic data.

### Two-Dimensional Maps
RapBot is used to create two-dimensional maps of the explored space. Each time RapBot runs, it creates a new map. All maps are stored locally on disk. Maps contains metadata about the time the map was created. Each map encodes information about how points in a space are reachable from nearby points. One way to think about the maps, is to think of super0imposing a grid over a floor plan of an apartment. RapBot explores the apartment -- not knowing where in the grid it began -- and tracks which points on the grid it can access from its current point. This information lends itself well to a sparse graph format. Where each point on the grid is a node and there is a vertex between its neighboring points that RapBot can access.

### Heat Map
The heat map is a visualization of RapBot's exploration over time. RapBot aligns each of the two0dimensional maps stored on its filesystem and transforms change over time to the temperature range. Temperature is calculated by the relationship between the number of transitions between the node being accessible, and not, and the number of maps processed.

### Users
A user is any person with access to the web interfaces running on RapBot. On start, RapBot will launch an interface to view heat maps, historic maps, and the current map.
