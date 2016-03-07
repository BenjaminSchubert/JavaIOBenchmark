#!/usr/bin/python3
# -*- Coding : utf-8 -*-

import csv
from collections import namedtuple
import os
import sys

from matplotlib import pylab

import matplotlib.pyplot as plt
import matplotlib.patches as mpatches

pylab.rcParams['ytick.major.pad'] = '10'


__author__ = 'Benjamin Schubert, benjamin.schubert@epfl.ch'


color_list = ["blue", "red", "green", "gray", "purple", "black"]

Point = namedtuple("point", ["blocksize", "duration"])


def read_data(_file):
    data = {
        "read": {
            "buffered": [],
            "unbuffered": []
        },
        "write": {
            "buffered": [],
            "unbuffered": []
        }
    }

    with open(_file, newline="") as f:
        cvs_data = csv.DictReader(f)

        for entry in cvs_data:
            strategy = "unbuffered" if "Without" in entry["strategy"] else "buffered"
            data[entry["operation"].lower()][strategy].append(Point(entry["blockSize"], entry["durationInMs"]))

    return data


def create_graph(data, name, limit=None):
    fig, ax = plt.subplots(figsize=(10, 4))
    legends = []
    legend_labels = []
    colors = color_list.copy()

    for operation in data.keys():
        for strategy in data[operation].keys():
            x_axis = []
            y_axis = []
            for point in data[operation][strategy]:
                if limit is None or int(point.blocksize) < limit:
                    x_axis.append(point.blocksize)
                    y_axis.append(point.duration)

            color = colors.pop()
            ax.plot(x_axis, y_axis, marker="o", linewidth=3, label="Overhead", color=color)
            legends.append(mpatches.Patch(color=color, label=strategy + " " + operation))
            legend_labels.append(strategy + " " + operation)


    ax.set_yscale("log")
    ax.set_xlabel("Taille du bloc de lecture", size=20)
    ax.set_ylabel("Temps total [ms]", size=20)

    for label in (ax.get_xticklabels() + ax.get_yticklabels()):
        label.set_fontsize(16)

    fig.legend(handles=legends, labels=legend_labels, prop={"size": 18})

    plt.tight_layout()
    os.makedirs("./report/", exist_ok=True)
    plt.savefig("./report/{}.eps".format(name))


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("No file with data was specified\n[Usage] graph.py file")
    data = read_data(sys.argv[1])
    create_graph(data, "graph-complete")
    create_graph(data, "graph-start", 128)
