#!/usr/bin/env python

import os
from fabric.api import *  # noqa
from fabric.colors import green, yellow
from fabric.contrib.console import confirm

from prettyprint import pp
import re

VERSION_PATTERN = r'^v\d+(\.\d+)+?$'
env.releases_directory = "release"
env.root_dir = os.path.abspath(os.path.dirname(__file__))
env.release = "HEAD"

proxy = os.environ.get('http_proxy', None)
env.http_proxy = env.http_proxy_port = None
if proxy is not None:
    env.http_proxy, env.http_proxy_port = proxy.rsplit(":")


def latest_git_tag():
    description = local('git describe master', capture=True).rstrip('\n')
    if '-' in description:
        latest_tag = description[:description.find('-')]
    else:
        latest_tag = description
    if not re.match(VERSION_PATTERN, latest_tag):
        latest_tag = None
    return latest_tag


def compare_versions(x, y):
    """
    Expects 2 strings in the format of 'X.Y.Z' where X, Y and Z are
    integers. It will compare the items which will organize things
    properly by their major, minor and bugfix version.
    ::

        >>> my_list = ['v1.13', 'v1.14.2', 'v1.14.1', 'v1.9', 'v1.1']
        >>> sorted(my_list, cmp=compare_versions)
        ['v1.1', 'v1.9', 'v1.13', 'v1.14.1', 'v1.14.2']

    """
    def version_to_tuple(version):
        # Trim off the leading v
        version_list = version[1:].split('.', 2)
        if len(version_list) <= 3:
            [version_list.append(0) for _ in range(3 - len(version_list))]
        try:
            return tuple((int(version) for version in version_list))
        except ValueError:  # not an integer, so it goes to the bottom
            return (0, 0, 0)

    x_major, x_minor, x_bugfix = version_to_tuple(x)
    y_major, y_minor, y_bugfix = version_to_tuple(y)
    return (cmp(x_major, y_major) or cmp(x_minor, y_minor)
            or cmp(x_bugfix, y_bugfix))


def make_tag():
    if confirm(yellow("Tag this release?"), default=True):
        print(green("The last 5 tags were: "))
        tags = local('git tag | tail -n 20', capture=True)
        pp(sorted(tags.split('\n'), compare_versions, reverse=True))
        prompt("New release tag in the format vX.Y[.Z]?", 'tag',
               validate=VERSION_PATTERN)
        local('git tag -as %(tag)s' % env)
        local('git push origin', capture=True)
        local('git push --tags origin', capture=True)
        local('git fetch --tags origin', capture=True)


@task
def release():
    make_tag()


@task
def sequence_diagrams():
    with lcd("docs/sequences"):
        local("make")


def release_descriptor(path):
    with lcd(path):
        return local('git describe HEAD', capture=True).rstrip("\n")
