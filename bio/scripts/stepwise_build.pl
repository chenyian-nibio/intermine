#!/usr/bin/perl
# 2011/3/1 chenyian
# Script for stepwise building production database
# The script have to execute from the base directory of the project, which contains project.xml
# syntax:
# $0 									// build source from beginning to end
# $0 from=[source_1] to=[source_2]		// build source from source_1 to source_2
# $0 from=[source_1]					// build source from source_1 to end
# $0 to=[source_2]						// build source from beginning to source_2
#
# NOTICE:
# The script doesn't initialize the database, and doesn't execute the post-processing steps.  
#
# 2011/4/15 chenyian
# The following procedures were add:
# 1. Back up when the integration has been done.
# 2. Run post processing
# 3. Back up the complete production database  
#
# !Following arguments are not test yet! (2010/4/20)
# step=n; n = 1: dump after the sources integrated;
# n = 2: do post processing; n = 3: dump after post processing 

use XML::DOM;
use Data::Dumper;
use Net::SMTP;

my $integrate = "integrate";
my $projectXmlFile= "project.xml";

my $antCmd = "/usr/local/ant/bin/ant -v ";

my $integrateDir = "integrate";
my $postprocessDir = "postprocess";

# Need to be customized
my $databaseName = "production-target-chen";
my $databaseUser = "intermine";
my $backupPath = "/scratch/targetmine";
#my $dumpCmd = "pg_dump -U $databaseUser $databaseName > $backupPath";
#my $dumpCmd = "pg_dump -v -F c -U $databaseUser -f $backupPath";

#if ($#ARGV > 1) {
#	die "Invalid arguments; too may arguments.";
#}
my %argvs;
foreach (@ARGV) {
	my ($key, $value) = split /=/,$_;
#	if ($key ne "from" && $key ne "to") {
#		die "Invalid arguments; $_";
#	}
	$argv{$key} = $value;
}

if (-e $projectXmlFile) {
#    print("Found $projectXmlFile\n");
} else {
    die "Can't find a file named $projectXmlFile check that you are in the root directory of the chosen mine.\n";
}

my $dbhost = "";
if ($argvs{'hostip'}) {
	$dbhost = " -h $argvs{'hostip'}";
}
my $dumpCmd = "pg_dump -v -F c $dbhost -U $databaseUser -f $backupPath";

my $parser = new XML::DOM::Parser;
my $doc = $parser->parsefile ($projectXmlFile);

my $result = buildCommandsForTag("source", "integrate", "-Dsource=");

$result = sliceAssignedSource($result);

# change directory to integrate/
chdir($integrateDir);
executeCommand($result);

#print Data::Dumper->Dump($result);
#print "end of script.\n";
sendNoticeMail("Build finished","The production database has been successfully built.");

if ($argv{'step'} > 0) {
	print "Start dumping production database...\n";
	dumpDatabase("all.src");
	print "Finish dumping production database...\n";
}
if ($argv{'step'} > 1) {
	print "Run postprocessing...\n";
	chdir("../$postprocessDir");
	executePostprocessing();
	sendNoticeMail("Post processing finished","The post processing has been successfully done.");
}
if ($argv{'step'} > 2) {
	print "Start dumping production database...\n";
	dumpDatabase("complete");
	print "Finish dumping production database...\n";
}
exit;

sub executePostprocessing {
	my $cmd = "$antCmd";
	open PIPE, "$cmd |" or die "cannot run $cmd: $?\n";
	while (<PIPE>) {
		print STDOUT " [post] $_";
	}
	close PIPE;
	if ($? != 0) {
		sendNoticeMail("Post process Failed","Post process failed, check the build log.");
		die "failed with exit code $?: @_\n";
	}
}

sub dumpDatabase {
	my @t = localtime();
	my $date = sprintf("%02d", $t[5] % 100).sprintf("%02d", $t[4]+1).sprintf("%02d", $t[3]);
	my $time = sprintf("%02d", $t[2]).sprintf("%02d", $t[1]).sprintf("%02d", $t[0]);
#	my $cmd = "$dumpCmd/$_[0].$date.$time.auto.pgdump";
	my $cmd = "$dumpCmd/$_[0].$date.$time.auto.backup $databaseName";

	open PIPE, "$cmd |" or die "cannot run $cmd: $?\n";
	while (<PIPE>) {
		print STDOUT " [dump] $_";
	}
	close PIPE;
	if ($? != 0) {
		sendNoticeMail("Dump Failed","Dumping the production database encounter some errors.");
		die "failed with exit code $?: @_\n";
	}
}

sub executeCommand {
	foreach my $source(@{$_[0]}) {
		my $cmd = $source->{"cmd"};
		print "Executing...",$cmd,"\n";
		print STDOUT `date`, "\n\n";
	    open PIPE, "$cmd |" or die "can't run $cmd: $?\n";
		while (<PIPE>) {
			print STDOUT " [$source->{'tag'}] $_";
		}
		print STDOUT `date`, "\n\n";
		print $cmd," finished\n\n";
	    close PIPE;
	    if ($? != 0) {
	    # build failed? send a mail...
	    	sendNoticeMail("Build Failed","Building the production database encounter some errors, while integrating the data source: $source->{'tag'} .");
	    	die "failed with exit code $?: @_\n";
	    }
	}
}

sub sendNoticeMail {
	my ($subject, $content) = @_;
	my $smtp = Net::SMTP->new(Host=>'127.0.0.1', Hello=>'localhost', Debug=>1,);
	
	$smtp->mail('chenyian@nibio.go.jp');
	$smtp->to('chenyian@nibio.go.jp');
	
	$smtp->data();
	$smtp->datasend("To: chenyian\@nibio.go.jp\n");
	$smtp->datasend("Subject: $subject\n");
	$smtp->datasend("\n");
	$smtp->datasend("$content\n");
	$smtp->dataend();
	
	$smtp->quit;
}

sub sliceAssignedSource {
	my @sliced = ();
	my $flag = 1;
	my $from_set = 0;
	my $to_set = 0;
	if (defined $argv{'from'}) {
		$flag = 0;
		$from_set = 1;
	} 
	if (defined $argv{'to'}) {
		$to_set = 1;
	} 
	foreach (@{$_[0]}) {
		if (!$flag && $_->{'tag'} eq $argv{'from'}) {
			$flag = 1;
			$from_set = 0;
		}
		if ($flag) {
			push @sliced, $_;
		}
		if ($flag && $_->{'tag'} eq $argv{'to'}) {
			$flag = 0;
			$to_set = 0;
		}
	}
	if ($from_set) {
		die "Wrong source name: ",$argv{'from'},"\n";
	}
	if ($to_set) {
		die "Wrong source name: ",$argv{'to'},"\n";
	}
	
	return \@sliced;
}

sub buildCommandsForTag {

  my $tagAlias = $_[0];
  my $stageDir = $_[1];
  my $cmdStub = $_[2];

  unless (defined $tagAlias and defined $cmdStub) {
    die "Need to supply both a tag name and a cmd stub!";
  }

  my $tags = $doc->getElementsByTagName("$tagAlias");
  my @cmds;

#  printStdOut("TAG: $tagAlias has this many occurances:" . $tags->getLength . "\n");

  for (my $i = 0; $i < $tags->getLength; $i++)
  {
    my $tag = $tags->item ($i);
    my $tagName = $tag->getAttributeNode ("name")->getValue;
    my $cmd = $antCmd . "$cmdStub$tagName";

    #Check to see if we have encountered a dump or index instruction.
    my $dump = $tag->getAttributeNode("dump");
    my $index = $tag->getAttributeNode("index");
    push @cmds, {"dir" => $stageDir, "cmd" => $cmd, "tag" => $tagName,
                 "dmp" => (defined $dump ? 1 : 0), "idx" => (defined $index ? 1 : 0) };
  }
  return \@cmds;
}
