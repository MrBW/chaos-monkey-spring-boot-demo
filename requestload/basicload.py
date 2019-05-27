import subprocess
import logging

import sys

logging.basicConfig(stream=sys.stdout, level=logging.INFO)


def initialize_load():
    urls = ["http://gateway:8080/startpage/", "http://gateway:8080/startpage/cb"]
    logging.info(
        "initializing basic request load for " + ", ".join(str(url) for url in urls)
    )

    subprocess.run(["sleep", "90"])

    while True:
        for url in urls:
            process = subprocess.Popen(["ab", "-n", "20000", "-c", "2", url])
        process.wait()


if __name__ == "__main__":
    initialize_load()
