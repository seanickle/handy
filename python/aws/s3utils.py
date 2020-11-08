import boto3
import os
import pandas as pd
from functools import reduce, partial

try:
    from StringIO import StringIO #python2
except:
    from io import StringIO #python3


def make_s3_resource():
    # access_key, secret = os.getenv('MY_ACCESS_KEY_ID'), os.getenv('MY_SECRET_ACCESS_KEY')
    access_key = None
    f = partial(boto3.resource, 's3',
            region_name='us-east-1')
    if access_key:
        return f(aws_access_key_id=access_key, aws_secret_access_key=secret)
    else:
        return f()


def write_s3_file(bucket_name, s3fn, content, content_type=None):
    s3conn = make_s3_resource()
    put = partial(
            s3conn.Object(bucket_name, s3fn).put, 
            Body=content)

    if content_type:
        put(ContentType=content_type)
    else:
        put()


def read_s3_file(bucket_name=None, s3fn=None, s3uri=None):
    if s3uri is not None:
        bucket_name, s3fn = s3uri_to_parts(s3uri)
    s3conn = make_s3_resource()
    # try:
    return s3conn.Object(bucket_name, s3fn).get()["Body"].read()
    # except botocore.exceptions.ClientError as e:


def list_files(bucket_name, prefix):
    s3conn = make_s3_resource()
    my_bucket = s3conn.Bucket(bucket_name)
    out_vec = []
    for x in my_bucket.objects.filter(Prefix=prefix):
        out_vec.append(x)
    return out_vec


def s3uri_to_parts(s3uri):
    parts =  s3uri.split('/')
    return parts[2], '/'.join(parts[3:])


def s3_csv_to_df(bucket_name, s3fn):
    blah = read_s3_file(bucket_name, s3fn)
    foo = StringIO(blah.decode("utf-8"))
    return pd.read_csv(foo)


def big_s3_csv_to_df(bucket_name, s3fn_prefix, suffixes):
    filenames = [s3fn_prefix + suff
            for suff in suffixes]
    # return filenames
    parts = [read_s3_file(bucket_name, s3fn) 
            for s3fn in filenames ]
    blah = reduce(lambda x, y: x+y, parts)
    foo = StringIO(blah.decode("utf-8"))
    return pd.read_csv(foo)


def df_to_s3(bucket_name, df, s3fn, index=False):
    s = StringIO()
    df.to_csv(s, index=index)
    write_s3_file(bucket_name, s3fn, content=s.getvalue())


def copy_s3_to_local(s3uri, local_loc, force=False):
    if not force:
        assert not os.path.exists(local_loc)

    (bucket_name, s3fn) = s3uri_to_parts(s3uri)
    blah = read_s3_file(bucket_name, s3fn)
    with open(local_loc, 'wb') as fd:
        fd.write(blah)
