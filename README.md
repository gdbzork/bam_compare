## Bam Comparator

Do alignment packages really find all the alignments that are present in the
genome?  This package compares two BAM files, typically from different
aligners, to see if they found equivalent results.  Normal usage is to set
the aligners' parameters to find all equally-good best matches, then
compare them to see if they found the same set.  (In an ideal world they
would, of course.)  For simplicity, this version of the package only considers
exact matches.
