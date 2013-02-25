#!/usr/bin/perl

use strict;

my $base_path="/data/bio/db/Targetmine";

my $web_path="resources/webapp";


my @files = `ls $base_path/*/*current_ver.txt`;

my @time = localtime(time);
my $year = 1900 + $time[5];
my $month = sprintf "%02d", ($time[4] + 1);
my $date = sprintf "%02d", $time[3];


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
foreach my $file(@files) {
	open FILE,"$file", or die "$! : $file";
	my %map = ();
	while (<FILE>) {
		chomp;
		my ($name, $value) = split "=", $_;
		$map{$name} = $value;
	}
	close FILE;
	if ($map{'data_set'}) {
		$current_ver{$map{'data_set'}} = \%map;
	}
}

#foreach my $s(keys %current_ver) {
#	print $s,"\t",$current_ver{$s}{'url'},"\n";
#}
#exit;

open SAVE,">$web_path/news/$year/$month/index.html" or die $!;
open TEMPLATE,"release_info_template" or die $!;
while (my $line = <TEMPLATE>) {
	if ($line =~ "release_info_table") {
		#create table header here
		
		foreach my $s(sort keys %current_ver) {
			print SAVE "<tr>";
			if ($current_ver{$s}{'url'}) {
				print SAVE "<td class=\"leftcol\"><a href=\"$current_ver{$s}{'url'}\" target=\"blank\">$current_ver{$s}{'data_set'}</a></td>";
			} else {
				print SAVE "<td class=\"leftcol\">$current_ver{$s}{'data_set'}</td>";
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
		
		# create table footer here
	} else {
		print SAVE $line;
	}
}
close TEMPLATE;
close SAVE;
