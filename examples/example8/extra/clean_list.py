import sys
import argparse


# predefined tokens
#
multi_word_split_tokens = [';', ', AND ', ', ', ' AND ']
discard_rest_tokens = [' WITH ', ' W/ ', ' IN ']
replace_tokens = [':']


# helper parsing functions
#
def split_word(word, separators, split_separator='^'):
	for sep in separators:
		word = word.replace(sep, split_separator)
	return [w.strip() for w in word.split(split_separator)]

def trim_word(word, separators):
	for sep in separators:
		pos = word.find(sep)
		if pos != -1:
			word = word[:pos].rstrip()
	return word

def clean_word(word, cleanup_tokens, replace_token='-'):
	for c in cleanup_tokens:
		word = word.replace(c, replace_token)
	return word


# the main
#
if __name__ == "__main__":

	# parse the input arguments
	#
	parser = argparse.ArgumentParser(description='Parse the words')
	parser.add_argument('input', help='input file')
	parser.add_argument('--blacklist', help='blacklist file')
	parser.add_argument('--gate', action="store_true", help='output in GATE ANNIE GAZETTEER format')
	args = parser.parse_args()


	# load the blacklist
	#
	blacklist = []
	if args.blacklist != None:
		with open(args.blacklist) as in_file:
			blacklist = [line.rstrip().lower() for line in in_file]


	# begin parsing
	#
	clean_names = []
	aux_names = set()

	with open(args.input) as in_file:
		for line in in_file:
			line = line.rstrip('\n')

			# first split the list
			#
			words_orig = split_word(line, multi_word_split_tokens)

			# trim the non necessary parts
			#
			words_trimmed = [trim_word(w, discard_rest_tokens) for w in words_orig]

			# cleanup word
			#
			words_clean = [clean_word(w, replace_tokens) for w in words_trimmed]

			
			clean_names += words_clean

			for w in words_clean:
				names = w.split()
				if len(names) > 0 and names[0] not in aux_names:
					if names[0].lower() not in blacklist:
						aux_names.add(names[0])


	# print the results
	#
	full_list = aux_names | set(clean_names)

	if args.gate is True:
		for s in sorted(full_list):
			print "%s:name=%s" % (s, s)
	else:	
		for s in sorted(full_list):
			print s
