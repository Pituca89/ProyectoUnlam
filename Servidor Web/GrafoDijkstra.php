<?php
function dijkstra($grafo,$source,$target){
    $vertices =  array();
    $vecinas = array();
    $ruta = array();

    foreach($grafo as $borde){
        array_push($vertices,$borde[0],$borde[1]);
        $vecinas[$borde[0]][] = array('bordeFin' => $borde[1], 'costo' => $borde[2]);
    }
    $vertices = array_unique($vertices);

    foreach($vertices as $ver){
        $dist[$ver] = INF;
        $previo[$ver] = NULL;
    }

    $dist[$source] = 0;
    $g = $vertices;
    while(count($g) > 0){
        $min = INF;
        foreach($g as $ver){
            if($dist[$ver] < $min){
                $min = $dist[$ver];
                $u = $ver;
            }
        }

        $g = array_diff($g, array($u));
        if($dist[$u] == INF or $u == $target){
            break;
        }

        if(isset($vecinas[$u])){
            foreach($vecinas[$u] as $arr){
                $alt = $dist[$u] + $arr['costo'];
                if($alt < $dist[$arr['bordeFin']]){
                    $dist[$arr['bordeFin']] = $alt;
                    $previo[$arr['bordeFin']] = $u;
                }
            }
        }
    }

    $u = $target;
    while(isset($previo[$u])){
        array_unshift($ruta,$u);
        $u = $previo[$u];
    }
    array_unshift($ruta,$u);
    return $ruta;
}
?>