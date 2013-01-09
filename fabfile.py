#!/usr/bin/env python

import os
import re
import boto.s3.key
import boto.s3.connection
from prettyprint import pp
from fabric.api import *
from fabric.utils import abort, puts
from fabric.colors import green, yellow
from fabric.contrib.console import confirm

env.aws_access_key = os.environ['AWS_ACCESS_KEY_ID']
env.aws_secret_key = os.environ['AWS_SECRET_ACCESS_KEY']
env.s3_bucket = "openxcplatform.com"

env.root_dir = os.path.abspath(os.path.dirname(__file__))
env.release = "HEAD"

proxy = os.environ.get('http_proxy', None)
env.http_proxy = env.http_proxy_port = None
if proxy is not None:
    env.http_proxy, env.http_proxy_port = proxy.rsplit(":")

VERSION_PATTERN = r'^v\d+(\.\d+)+?$'
env.releases_directory = "release"

def latest_git_tag():
    description = describe_version()
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
        except ValueError: # not an integer, so it goes to the bottom
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
        local('git push --tags origin', capture=True)
        local('git fetch --tags origin', capture=True)

@task
def release():
    make_tag()
    env.release = release_descriptor(env.root_dir)
    print(green("Using version %(release)s" % env))
    local("mvn clean")
    local("mvn package -pl enabler -am")
    upload_release("%(root_dir)s/enabler/target/openxc-enabler.apk" % env,
            "openxc-enabler-%s.apk" % env.release)

@task
def snapshot():
    local("mvn clean deploy -pl openxc -am")


def upload_release(target_file, s3_key):
    conn = boto.s3.connection.S3Connection(env.aws_access_key,
            env.aws_secret_key, proxy=env.http_proxy,
            proxy_port=env.http_proxy_port)
    bucket = conn.get_bucket(env.s3_bucket)
    key = boto.s3.key.Key(bucket)
    key.key = s3_key
    key.set_contents_from_filename(target_file)
    key.make_public()
    print ("Uploaded release as public to http://s3.amazonaws.com/%s/%s" %
            (bucket.name, s3_key))


def release_descriptor(path):
    with lcd(path):
        return local('git describe HEAD', capture=True).rstrip("\n")
