# Automated Bacterial Assembler
GUI for bacterial genome assembly

Offers a GUI for the sake of in-house analysis of raw sequence files (fastq or fastq.gz) from Illumina sequencing services. Three
options are available: Run FastQC, Quick Assembly, and Assemble Genome. 

# Dependencies:

Software:
- Java
- FastQC (https://www.bioinformatics.babraham.ac.uk/projects/fastqc/)
- BBTools (https://jgi.doe.gov/data-and-tools/bbtools/)
- Prodigal (https://github.com/hyattpd/Prodigal/releases)

*Run FastQC*:  
Input a directory containing raw read files, input a directory to output files to, and a number of threads to use.
Returns html files per sample with the QC statistics of associated reads. 

*Quick Assembly*:  
Input a directory containing raw read files, input a directory to output files to, and a number of threads to use. Runs the Tadpole assembler of the BBTools package ont he raw read files for extremely crude assemblies. Runs very quickly, 
but will return very crude contigs. 

*Assemble Genome*:    
Input a directory containing raw read files, input a directory to output files to, and a number of threads to use. Runs a modified version of the Bacteria Genome Assembly pipeline which can be found at: 
https://github.com/duceppemo/bacteria_genome_assembly/blob/master/illuminaPE_parallel_assemblies.sh
Note that many steps have been removed, as they are based on extra dependencies which are not completely necessary. Also, 
the assembler used is Tadpole, instead of Unicycler. Tadpole runs extremely quickly, but spends no time trying to resolve and
bridge highly repetitive regions, and so will produce a much more fragmented assembly than Unicycler. However, all contained
sequences are still represented. 
