#!/usr/bin/perl

# script to download the latest uniprot release into a directory like uniprot/5.3
# the version number is extracted from the relnotes.txt file on the FTP server
# the script should be run in svn/data/bio/scripts/

# chenyian 2009.7.10
# modify script to process pre-downloaded uniprot data

use strict;
use warnings;

#BEGIN {
  # find the lib directory by looking at the path to this script
#  push (@INC, ($0 =~ m:(.*)/.*:)[0] . '/../../intermine/perl/lib/');
#}
#use InterMine::DataDownloader;

my $logdir="/home/chenyian/data/";
my $tempname="split_uniprot_log.txt";
my $logname=$logdir.$tempname;
#my $home="/home/chenyian/data";
my $home="/data/bio/db/Targetmine";
my $config_file = "../../targetmine/project.xml";

if (@ARGV!=4) {   
    print "no arguments passed, using default settings\n";
} else {
    $logdir = $ARGV[0];
    $logname= $ARGV[1];
    $home = $ARGV[2];
    $config_file = $ARGV[3];
}

#my $download_dir = "/home/chenyian/data/uniprot";
my $download_dir = "/data/bio/db/uniprot";
#my $download_dir = "/scratch/chenyian/uniprot";


#============================================================================

#splits the uniprot xml files into separate files based on the ncbi taxon id. Reads the taxons of interest from a
#centrally stored file.

my($start, $end, $buffer) = ("<entry", "</entry>", '');
my ($filename, $fname_end);
my ($keep, $taxon) = ("false", '0000');
my (@new_contents, @old_contents);

#the xml declaration and root element for each output file
my $prolog = '<?xml version="1.0" encoding="UTF-8"?>
<uniprot xmlns="http://uniprot.org/uniprot"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
xsi:schemaLocation="http://uniprot.org/uniprot http://www.uniprot.org/support/docs/uniprot.xsd">';
#define the last elements for each output file
my $element_end = '<copyright>
Copyrighted by the UniProt Consortium, see http://www.uniprot.org/terms
Distributed under the Creative Commons Attribution-NoDerivs License
</copyright>
</uniprot>';

my $split_dir = "$home/uniprot/split/";
if (&checkdir_exists($split_dir)==0) {
  warn "Data already split.\n";
} else {
  print "Splitting files by taxon Id\n";
  #use hash to define output name for each source file
  #comment out one of them to process the other file only
  my %files = (
               'sprot'  => {    'filename' => "$download_dir/uniprot_sprot.xml",
                                'fname_end' => '_uniprot_sprot.xml'},
               'trembl'  => { 'filename' => "$download_dir/uniprot_trembl.xml",
                              'fname_end' => '_uniprot_trembl.xml'},
              );

  #open file to get taxonIDs from the config file
  my %taxons = &get_taxonIds($config_file,"uniprot.organisms");
  my $size = %taxons;
  if ($size eq 0) {
      die "no taxonIds!  please add the taxonIds to the uniprot entry in the project.xml file\n";
  }


  # set to 1 while we are inside an <organism>...</organism>
  my $in_organism = 0;

  #open each uniprot file
  foreach my $file_type (sort keys %files) {
    $filename = $files{$file_type}->{'filename'};
    $fname_end = $files{$file_type}->{'fname_end'};

    #read a line at a time and identify the start (<entry), stop (</entry), and taxon id (type="NCBI Taxonomy)
    open(F,"<$filename") or die "$! was expecting something like uniprot_sprot.xml or uniprot_trembl.xml";
    while (my $newline = <F>) {
      #if a new entry is found we might want to keep it
      if ($newline =~ /<organism>/ or $newline =~ /<organism .+>/) {
        $in_organism = 1;
      } elsif ($newline =~ m!</organism>!) {
        $in_organism = 0;
      } elsif ($newline =~ /$start/gi) {
        $keep = "true";
        #is this a taxon Id line?
      } elsif ($newline && $in_organism && 
#               $newline=~/dbReference type="NCBI Taxonomy".*id="(\d+)"/) { # chenyian: uniprot change the format, 20110114 
               $newline=~/dbReference id="(\d+)".*type="NCBI Taxonomy"/) {
        #get the taxon Id only if we are in an <organism>...</organism> to avoid <organismHost>
        $taxon = $1;
        #is this the end of the entry?
      } elsif ($newline && $newline =~ /$end/gi) {
        #add to the buffer
        $buffer .= $newline;
        #if we want this species, write the buffer to a file
        if (exists $taxons{$taxon}) {
          &writefile($buffer,$taxon,$fname_end);
        }
        #reset everything ready for the next entry element.
        $keep = "false";
        $buffer = "";
        $taxon = 0000;
      }
      #while keep is true, build up the buffer until the element end is found
      if ($keep eq "true") {
        $buffer .= $newline;
      }
    }
    close(F) or die "$!";
  }

  #identify all the output files once the XML processing is finished
  opendir(DIR,$split_dir) || die("Cannot open directory !\n");
  @new_contents=grep(!/^\.\.?$/,readdir DIR);
  closedir(DIR);

  #add the copyright and close the root element for each output file
  foreach my $file (@new_contents) {
    $file = $split_dir.$file;
    open( FILE, ">>$file") or die "cannot open $file: $!";
    print FILE "$element_end";
    close(FILE);
  }
#  system "chmod -R a+r,g+w $download_dir";
}

#creates files and adds elements as appropriate
sub writefile(){
  my ($xml,$species,$end) = @_;
  my $new_file = $split_dir.$species.$end;

  #if this is a new species file, create it and add the
  #xml declaration, open the root element and the species entry
  if (!-e $new_file ) {
    open(FH, ">$new_file") || die "$!";
    print "create $new_file ...\n"; # for tracing
    print FH "$prolog\n";
    print FH "$xml";
    close(FH);
  } else {
    #if the file already exists, append the species entry
    open(FH, ">>$new_file") || die "$!";
    print FH "$xml";
    close(FH);
  }
}

#get taxon Ids from file
sub get_taxonIds(){
    my ($file,$trigger) = @_;
    # parse file looking for this line: <property name="uniprot.organisms" value="7955 9606"/>                                                                                                                 
    open(F,"<$file") or die "$! [$file]";
    my @projectxml = <F>;
    my @lines = grep(/$trigger/, @projectxml);
    close(F) or die "$!";

    my $line = $lines[0];
    my $i = index($line, 'value="') + 7;
    my $valueSubstr = substr $line, $i;
    my $locationSecondQuotation = index($valueSubstr, '"');
    my $taxonIds = substr $valueSubstr, 0, $locationSecondQuotation;
    print "processing $taxonIds\n";
    my @orgArray = split(/ /, $taxonIds);
    my %orgHash;
    @orgHash{@orgArray} = (1) x @orgArray;
    return %orgHash;
}
#check if a directory exists and return 0 if it does,
#create it and return 1 if it doesn't
sub checkdir_exists(){
	my $dir = shift;
	if (!(-d $dir)) {
	    print STDERR "creating directory: $dir\n";
	    mkdir $dir
	        or die "failed to create directory $dir :",$!;
		return 1;
	}else{
		print STDERR "$dir exists\n";
		return 0;
	}
}
