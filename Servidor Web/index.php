<?php
include "config.php";

$array = array(
    "user"=>"matias",
    "limites"=> array(
        "origen" => "A",
        "destino" => "Z",
    ),
    "ruta" => "F20D3I50F22"
);

$resultado = json_encode($array);

switch($_POST["OPERACION"]){
    case CONEXION: 
        //$_POST["CHIPID"]; //Aca iria el codigo para registrar el id de la plataforma a la base de datos
        echo "ok";
        break;
    case REGISTRO_USUARIO:       
        echo  $_POST["USERID"]; //Aca iria el codigo para registrar el id de la plataforma a la base de datos
        break;
    case INICIAR:       
        echo  "Esperando Orden de usuario"; //Aca iria el codigo para cargar las rutas en la plataforma
        break;
    default: 
        echo "Codigo invalido";
}

?>