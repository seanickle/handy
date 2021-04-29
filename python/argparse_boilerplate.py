import argparse

def bake_options():
    return [
            [['--verbose', '-v'],
                {'action': 'store_true',  # stores boolean
                    'help': 'pass to to be verbose with commands'},
                ],
            [['--dry-run', '-D'],
                {'action': 'store_true',
                    'help': 'Dry run. Just print the command.  '},
            ],
            [['--color', '-c'],
                {'action': 'store',  # Stores the value given
                    'help': 'Dry run. Just print the command.  '},
            ],
                ]
    ##
    #             help='',
    #             default='',
    #             required='',
    #             choices='',
    #             action='',
    #             type='',
            

def do():
    parser = argparse.ArgumentParser()

    [parser.add_argument(*x[0], **x[1])
            for x in bake_options()]

    # Collect args from user.
    args = parser.parse_args()

    print vars(args)
    
do()
