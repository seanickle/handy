
import argparse
import datetime
import pytz
import pandas as pd

def utc_ts():
    utc_now = datetime.datetime.utcnow().replace(tzinfo=pytz.UTC)
    return utc_now.strftime('%Y-%m-%dT%H%M%S')

def bake_options():
    return [
            [['--verbose', '-v'],
                {'action': 'store_true',
                    'help': 'pass to to be verbose with commands.'},
                ],
            [['--dry-run', '-D'],
                {'action': 'store_true',
                    'help': 'Dry run. Just print the command.'},],

            [['--input-files', '-f'],
                {'action': 'store',
                    'help': 'Name of comma separated input files.'},],

            [['--left', '-l'],
                {'action': 'store',
                    'help': 'first file, when only two'},],

            [['--right', '-r'],
                {'action': 'store',
                    'help': 'second file, when only two '},],
            [['--out-dir', '-o'],
                {'action': 'store',
                    'help': 'where to write new files'},],
                ]
    ##
    #             help='',
    #             default='',
    #             required='',
    #             choices='',
    #             action='',
    #             type='',
    
def diff_files(files):
    sets = [[x.split('/')[-1], (readlines(x))] for x in files]
    
    header_line = sets[0][1][0]
    
    diff = [header_line] + list(set(sets[1][1]) - set(sets[0][1]))
    
    print(f"producing {sets[1][0]} minus {sets[0][0]}")
    return diff

    
def readlines(loc):
    with open(loc) as fd:
        return fd.readlines()
    
    
def write_lines(lines, loc):
    with open(loc, 'w') as fd:
        fd.writelines(lines)
            

def do():
    parser = argparse.ArgumentParser()

    [parser.add_argument(*x[0], **x[1])
            for x in bake_options()]

    # Collect args from user.
    args = parser.parse_args()
    args = vars(args)
    print(args)
    out_dir = args['out_dir']
    diff_lines = diff_files([args["left"], args["right"]])
    
    write_lines(diff_lines, loc=f'{out_dir}/{utc_ts()}-out.csv')
    
do()
