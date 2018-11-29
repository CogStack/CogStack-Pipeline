FROM cogstacksystems/cogstack-pipeline:latest

RUN apt-get update && \
	apt-get install -y curl && \
	apt-get clean autoclean && \
	apt-get autoremove -y && \
	rm -rf /var/lib/apt/lists/*

# GATE directories structure:
# - core components: /gate/home/
# - custom user apps: /gate/app/
WORKDIR /gate/

# for the moment use the older GATE bundle containing all plugins and core components 
# TODO: update to GATE 8.5+
RUN curl -L 'https://downloads.sourceforge.net/project/gate/gate/8.4.1/gate-8.4.1-build5753-BIN.zip?r=https%3A%2F%2Fsourceforge.net%2Fprojects%2Fgate%2Ffiles%2Fgate%2F8.4.1%2Fgate-8.4.1-build5753-BIN.zip' > gate-8.4.1-build5753-BIN.zip && \
	unzip gate-8.4.1-build5753-BIN.zip && \
	mv gate-8.4.1-build5753-BIN home && \
	rm gate-8.4.1-build5753-BIN.zip
ENV GATE_HOME=/gate/home

# switch to CogStack main directory
WORKDIR /cogstack
