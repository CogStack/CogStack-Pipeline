
FROM jupyter/datascience-notebook:latest

USER root

ENV HTTP_PROXY=$HTTP_PROXY
ENV HTTPS_PROXY=$HTTPS_PROXY
ENV NO_PROXY=$NO_PROXY

# install R
RUN apt-get update --allow-unauthenticated && apt-get upgrade -y && \
    apt-get install -y --allow-unauthenticated --no-install-recommends \
    gnupg-agent \
    ca-certificates \
    apt-transport-https \
    software-properties-common \
    fonts-dejavu \
    build-essential \
    python3-dev \
    python3-pip \
    unixodbc \
    unixodbc-dev \
    r-cran-rodbc \
    gfortran \
    gcc \
    git \
    ssh \
    jq \
    htop \
    wget \
    curl \
    r-base

RUN pip3 install --upgrade pip
RUN pip3 install virtualenv pytesseract ipyparallel py7zr cython isort html2text jsoncsv simplejson detect wheel elasticsearch nltk keras bokeh seaborn matplotlib graphviz plotly tqdm && pip3 install medcat && pip install ipywidgets jupyter jupyterlab && pip install jupyterhub-nativea$
RUN pip3 install dvc jupyter_contrib_core jupyter_contrib_nbextensions jupyter-server-proxy tensorflow fastbook
RUN jupyter labextension install @jupyterlab/server-proxy