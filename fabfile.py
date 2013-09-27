#!/usr/bin/env python

import os
import boto.s3.key
import boto.s3.connection
from fabric.api import *
from fabric.colors import green

env.aws_access_key = os.environ['AWS_ACCESS_KEY_ID']
env.aws_secret_key = os.environ['AWS_SECRET_ACCESS_KEY']
env.s3_bucket = "openxcplatform.com"

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
    upload_release("%(root_dir)s/enabler/target/openxc-enabler.apk" % env,
            "openxc-enabler-%s.apk" % env.release)
    # https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
    print(green("Deployed to a staging repository at Sonatype - now go "
        "to https://oss.sonatype.org, close the repository and release it."))

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
