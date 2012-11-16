#!/usr/bin/perl

use strict;

my $base_path="/data/bio/db/Targetmine";

my $web_path="resources/webapp";

my @sources = (
	"gene_info", "uniprot", "go-annotation/obo", "go-annotation", 
	"interpro", "kegg", "scop", "biogrid", "do", "do-annotation", 
	"drugbank", "enzyme", "reactome/lv3", "irefindex", "stitch", 
	"cath", "gene3d", "nci-pathway", "metazoan", "tfactor", "pubchem",
	"chebi","chembl");


my @time = localtime(time);
my $year = 1900 + $time[5];
my $month = sprintf "%02d", ($time[4] + 1);
my $date = sprintf "%02d", $time[3];

# print "path = /news/$year/$month/\n";

#check the folder
unless (-d "$web_path/news") {
	mkdir "$web_path/news";
}
unless (-d "$web_path/news/$year") {
	mkdir "$web_path/news/$year";
}
unless (-d "$web_path/news/$year/$month") {
	mkdir "$web_path/news/$year/$month";
}

# read in release info
my %current_ver = ();
foreach my $s(@sources) {
	open FILE,"$base_path/$s/current_ver.txt", or die "$!; $base_path/$s/current_ver.txt";
	while (<FILE>) {
		my ($name, $value) = split "=", $_;
		$current_ver{$s}{$name} = $value;
	}
	close FILE;
}


open SAVE,">$web_path/news/$year/$month/index.html" or die $!;
open TEMPLATE,"release_info_template" or die $!;
while (my $line = <TEMPLATE>) {
	if ($line =~ "release_info_table") {
		#create table header here
		
		foreach my $s(@sources) {
			print SAVE "<tr>";
			if ($current_ver{$s}{'url'}) {
				print SAVE "<td class=\"leftcol\"><a href=\"$current_ver{$s}{'url'}\" target=\"blank\">$current_ver{$s}{'data_source'}</a></td>";
			} else {
				print SAVE "<td class=\"leftcol\">$current_ver{$s}{'data_source'}</td>";
			}
			if ($current_ver{$s}{'version'}) {
				print SAVE "<td>$current_ver{$s}{'version'}</td>";
			} else {
				print SAVE "<td>-</td>";
			}
			if ($current_ver{$s}{'date_type'} && $current_ver{$s}{'date'}) {
				print SAVE "<td>$current_ver{$s}{'date_type'}:<br />$current_ver{$s}{'date'}</td>";
			} else {
				print SAVE "<td>-</td>";
			}
			
			print SAVE "</tr>\n";
		}
		# add pdb information here
		print SAVE "<tr><td class=\"leftcol\">PDB</td><td>-</td><td>Download Date:<br \>$year/$month/$date</td></tr>\n";
		
		# create table footer here
	} else {
		print SAVE $line;
	}
}
close TEMPLATE;
close SAVE;
