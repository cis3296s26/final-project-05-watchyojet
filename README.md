# WatchyoJet
**Autonomous Air Traffic Control Decision System**

WatchyoJet is a web-based autonomous air traffic controller decision system designed to manage complex operations, maintain aircraft separation, and optimize runway usage. By replacing traditional, human-centric ATC systems with an autonomous system, WatchyoJet automates the decision-making process to reduce human workload and prevent unforeseen errors. The system takes into account aircraft headings, altitude, speed, distance from the airport, runway availability, and weather forecasts to devise real-time, conflict-free ATC commands using priority-queue, constraint-based scheduling. 

![Proof Of Concept GUI:](POC.jpg)

## How to run
1. Download the latest release package from the **Releases** section on the right side of this GitHub page.
2. Uncompress the downloaded `.zip` or `.tar.gz` file.
3. To run the backend (Spring Boot), open your command line, navigate to the extracted folder, and execute the jar file:
   `java -jar watchyojet-backend.jar`
4. To run the frontend (React), navigate to the frontend folder and start the application:
   `npm start`
5. Open your web browser and navigate to `http://localhost:3000`. You will see the WatchyoJet interface on your screen.

## How to contribute
Follow our project board to see the backlog, current sprints, and the latest status of the project:
**Project Board:** [https://github.com/orgs/cis3296s26/projects/43/views/1]

## How to build
**Repository:** `https://github.com/cis3296s26/final-project-05-watchyojet.git`

We are using the `main` branch for our stable releases and cutting-edge development.

**Prerequisites / Required Resources:**
* **Backend:** Java (Spring Boot), MySQL Database.
* **Frontend:** React.
* **Websockets:** Spring websocket (backend) and Socket.IO (frontend).
* **Data Sources:** Flight Radar API, Public ADS-B datasets, OpenSky Network historical flight data.

**Building the Backend (Java/Spring Boot):**
1. Open your terminal and navigate into the backend directory.
2. Compile and build the project using Maven:
   `./mvnw clean install`
3. Run the Spring Boot application:
   `./mvnw spring-boot:run`

**Building the Frontend (React):**
1. Open a new terminal window and navigate into the frontend directory.
2. Install the required dependencies:
   `npm install`
3. Compile and run the React app:
   `npm start`

**What to expect when the app starts:**
The Spring Boot backend will initialize on port `8080`, establish a connection with the MySQL database, and open websocket channels for real-time communication. The React frontend will launch on port `3000` and automatically open in your default web browser, displaying the WatchyoJet autonomous tracking dashboard.
