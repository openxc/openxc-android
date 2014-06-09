#!/usr/bin/env python
"""Generate a sequence diagram using websequencediagrams.com

Thanks to websequencediagrams.com for the sample Python code for accessing their
API!

Usage:

    python generate_sequence_diagram.py input.txt output.png
"""
from __future__ import print_function
try:
    from urllib import urlencode
except ImportError:
    from urllib.parse import urlencode

try:
    from urllib import urlopen, urlretrieve
except ImportError:
    from urllib.request import urlopen, urlretrieve

import urllib
import re
import sys

def request_diagram(text, output_file, style='default'):
    request = {}
    request["message"] = text
    request["style"] = style
    request["apiVersion"] = "1"

    url = urlencode(request)
    url = url.encode('ascii')

    f = urlopen("http://www.websequencediagrams.com/", url)
    line = f.readline()
    f.close()

    expr = re.compile("(\?(img|pdf|png|svg)=[a-zA-Z0-9]+)")
    m = expr.search(line.decode('ascii'))

    if m == None:
        print("Invalid response from server.")
        return False

    urlretrieve("http://www.websequencediagrams.com/" + m.group(0),
            output_file)
    return True


def generate_diagram(input_filename, output_filename):
    with open(input_filename) as input_file:
        text = '\n'.join((line for line in input_file))
        request_diagram(text, output_filename, style="RoundGreen")


if __name__ == '__main__':
    generate_diagram(sys.argv[1], sys.argv[2])
