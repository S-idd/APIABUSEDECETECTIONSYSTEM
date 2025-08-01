Real-Time API Abuse Detection System on WSL
Overview
This project implements a real-time API abuse detection system on Windows Subsystem for Linux (WSL) using Kafka, Kong, and Python. The system is designed to monitor API traffic, ingest logs, process them for suspicious activity, and trigger actions (e.g., blocking abusive IPs) while providing monitoring and visualization capabilities.
Components and Workflow
Operating System

Ubuntu (WSL): The base environment where all components are hosted and executed.

Log Generation

Kong API Gateway: Acts as the entry point for API requests. It generates logs for all incoming API calls, capturing details like IP addresses, endpoints, and timestamps.
Fluent Bit: A lightweight log processor that collects logs from Kong. It is configured using a config.yaml file to filter and forward logs to Kafka. This ensures efficient log handling and routing.

Log Ingestion

Kafka: A distributed streaming platform that serves as the message broker. Logs from Fluent Bit are sent to a specific topic named kong-logs. Kafka ensures scalability and fault tolerance in log ingestion.
responder.py: A Python script that consumes messages from the kong-logs topic. It processes incoming log data and forwards it to the next stage for analysis.
Processor.py: A Python script that analyzes the log data from responder.py. It applies predefined rules or machine learning models to detect API abuse (e.g., excessive requests, unusual patterns). Based on the analysis, it sends signals to block or unblock logic.
Kong Admin API: Provides an interface to dynamically manage Kong configurations (e.g., adding or removing rate-limiting rules) based on the processor's output.

Monitoring & Visualization

Prometheus: A monitoring tool that collects metrics from the system (e.g., request rates, blocked IPs) and stores them for analysis. It integrates with the processor to track real-time performance.
Block/Unblock Logic: A decision-making component triggered by processor.py. If abuse is detected (e.g., an IP exceeds a threshold), it uses the Kong Admin API to block the offending IP. Conversely, it can unblock IPs when the threat subsides.
Grafana (optional): A visualization tool that connects to Prometheus to create dashboards. It provides graphical insights into API usage, abuse incidents, and system health.

Detailed Working

Log Generation: When an API request hits the Kong API Gateway, it generates detailed logs. These logs are captured by Fluent Bit, which is configured via config.yaml to parse and structure the data (e.g., extracting IP, endpoint, and timestamp).
Log Ingestion: Fluent Bit forwards the structured logs to Kafka’s kong-logs topic. The responder.py script subscribes to this topic, retrieves the logs, and passes them to processor.py for analysis.
Abuse Detection: The processor.py script evaluates the logs against abuse detection criteria (e.g., rate limits, anomaly detection). If an IP or request pattern is flagged as abusive, it triggers the block/unblock logic.
Action Execution: The block/unblock logic interacts with the Kong Admin API to enforce or lift restrictions (e.g., adding an IP to a deny list or removing it).
Monitoring: Prometheus scrapes metrics from the system (e.g., number of requests, blocked IPs) and makes them available for querying.
Visualization (Optional): Grafana pulls data from Prometheus to display real-time dashboards, helping administrators monitor API health and abuse trends.

Setup Instructions

Install Ubuntu on WSL:
Enable WSL on Windows and install Ubuntu from the Microsoft Store.


Set up Kong API Gateway:
Install Kong and configure it with your API services.
Enable logging plugins to capture API request details.


Install and Configure Fluent Bit:
Install Fluent Bit and edit config.yaml to specify log sources (Kong) and output (Kafka).


Install Kafka:
Set up a Kafka cluster and create the kong-logs topic.


Run Python Scripts:
Ensure Python is installed with required libraries (e.g., kafka-python).
Execute responder.py and processor.py in separate terminal sessions.


Configure Prometheus:
Install Prometheus and configure it to scrape metrics from the processor.


(Optional) Set up Grafana:
Install Grafana and connect it to Prometheus for dashboards.



Configuration

Fluent Bit: Edit config.yaml to define input (Kong logs) and output (Kafka topic).
Kong Admin API: Configure API endpoints and authentication in YAML config.yaml.
Processor.py: Adjust abuse detection thresholds or rules as needed.

Usage

Start all components in the order: Kong, Fluent Bit, Kafka, Python scripts, Prometheus, and Grafana (if used).
Monitor API traffic in real-time via Prometheus or Grafana dashboards.
Review logs and blocked IPs through the system outputs.

Contributing
Feel free to submit issues or pull requests on the repository. Contributions to improve detection algorithms or visualization are welcome.
License
MIT License
