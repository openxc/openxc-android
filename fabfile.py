#!/usr/bin/env python

import os
from fabric.api import *
from fabric.colors import green

env.root_dir = os.path.abspath(os.path.dirname(__file__))
env.release = "HEAD"

proxy = os.environ.get('http_proxy', None)
env.http_proxy = env.http_proxy_port = None
if proxy is not None:
    env.http_proxy, env.http_proxy_port = proxy.rsplit(":")

@task
def release():
    local("mvn release:clean")
    local("mvn release:prepare")
    local("mvn release:perform")
    local("mvn package -pl enabler -am")

    env.release = release_descriptor(env.root_dir)
    # https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
    print(green("Deployed to a staging repository at Sonatype - now go "
        "to https://oss.sonatype.org, close the repository and release it."))

@task
def snapshot():
    local("mvn clean deploy -pl openxc -am")

def release_descriptor(path):
    with lcd(path):
        return local('git describe HEAD', capture=True).rstrip("\n")
