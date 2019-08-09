<?php


function getSector(){
    $query = "call getSector;";
    if(!$result = mysqli_query($conn,$query)) die();

    $response = array();
    $response['opcion'] = 'SECTORES';
    $miarray = array();
    $i = 0;
    while($row = $result->fetch_array()){
        $miarray[$i] = $row;
        $i++;
    }
    $response['sectores'] = $miarray;
    mysql_close($conn);
    echo json_encode($response);
}

function verificar_conexion(){
    /**Podria agregar una prueba de conexion a la base de datos */
    $json = array(
        'opcion' => 'CONECTADO',//Enviar la ruta de prueba almacenada en la base de datos
    );
    $jsonEncode = json_encode($json);
    mysql_close($conn);
    echo $jsonEncode;
}

function login(){
    /**Validar usuario y contraseña */
    $json = array(
        'opcion' => 'USUARIO CONECTADO',//Enviar la ruta de prueba almacenada en la base de datos
    );
    $jsonEncode = json_encode($json);
    mysql_close($conn);
    echo $jsonEncode; 
}

function registrar_usuario(){
    /**Registrar usuario y contraseña */
    $json = array(
        'opcion' => 'USUARIO REGISTRADO',//Enviar la ruta de prueba almacenada en la base de datos
    );
    $jsonEncode = json_encode($json);
    mysql_close($conn);
    echo $jsonEncode; 
}

function obtener_ruta_prueba(){
    $query = "call getRutaPrueba;";
    if(!$result = mysqli_query($conn,$query)) die();

    $response = array();
    $response['opcion'] = 'RUTA';
    if($row = $result->fetch_object()){
        $response['ruta'] = $row->ruta;
    }
    mysql_close($conn);
    return json_encode($response);
}

function ruta_prueba(){
    $json = array(
        'opcion' => 'PRUEBA',//Enviar la ruta de prueba almacenada en la base de datos
        'codigo' => RUTA_PRUEBA
    );
    $jsonEncode = json_encode($json);
    curl_setopt($ch,CURLOPT_POSTFIELDS,$jsonEncode);
    $result = curl_exec($ch);
    echo $jsonEncode;
}

function registrar_plataforma($id,$ip){
    $query = "call registrarPlataforma('".$id."','".$ip."');";
    if(!$result = mysqli_query($conn,$query)) die();
    else {
        mysql_close($conn);
        return "ok";
    }
}

function obtener_ip_plataforma($id){
    $query = "call obtenerIP('".$id."');";
    $salida = 0;
    if(!$result = mysqli_query($conn,$query)) die();
    else{
        if($row = $result->fetch_object()){
            $salida = $row->ip;
        }
    }
    mysql_close($conn);
    return $salida;
}
function enviar_ruta_prueba($id){
    $ip = obtener_ip_plataforma($conn,$id);
    $ruta = 0;
    if($ip != 0){
        //$ruta = obtener_ruta_prueba($conn);
        $ruta = 1;
    }
    mysql_close($conn);
    return $ruta;
}
?>