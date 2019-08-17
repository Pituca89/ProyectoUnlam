<?php
/*
 * Author: doug@neverfear.org
 */
require("Dijkstra.php");
function runTest() {
	$g = new Graph();
	$g->addedge(1, 2, 4);
	$g->addedge(1, 3, 1);
	$g->addedge(2, 4, 74);
	$g->addedge(3, 5, 2);
	$g->addedge(5, 1, 12);
	$g->addedge(5, 2, 12);
    $g->addedge(2, 1, 4);
	$g->addedge(3, 1, 1);
	$g->addedge(4, 2, 74);
	$g->addedge(5, 3, 2);
	$g->addedge(1, 5, 12);
	$g->addedge(2, 5, 12);
	list($distances, $prev) = $g->paths_from(1);
	
	$path = $g->paths_to($prev, 4);
	
	print_r($path);
	
}
runTest();
?>