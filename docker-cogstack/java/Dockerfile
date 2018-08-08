FROM openjdk:8-slim

RUN apt-get update && \
	apt-get dist-upgrade -y && \
	apt-get install -y imagemagick --fix-missing  tesseract-ocr && \
	apt-get clean autoclean && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*
