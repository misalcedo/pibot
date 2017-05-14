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
