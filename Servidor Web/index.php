<?php
require_once "config.php";
//require_once "funciones.php";
require_once "conexion.php";

$db = BaseDatos::getInstancia();
if(file_get_contents("php://input")){
	$data = json_decode(file_get_contents("php://input"));
	switch($data->OPCION){
		case SECTORES://9 {"OPCION":9, ID:11569620}
			echo $db->getSector($data->ID);//obtengo el listado de sectores a seleccionar por el usuario	
	    break;
		case VERIFICAR_CONEXION://7 {"OPCION":7}
			echo $db->verificar_conexion();//verificar la conexion con el servidor
	    break;
		case CONECTAR://0 {"OPCION":0}
			echo $db->login();//codigo para verificar usuario y contraseña
		break;
		case REG_USUARIO://4 {"OPCION":4}
			echo $db->registrar_usuario(); //codigo para registrar usuario y contraseña
		break;
		case OBTENER_PLATAFORMA://10 {"OPCION":10}
			echo $db->obtener_plataforma();//obtengo el listado de plataformas a seleccionar por el usuario		
		break;
		case REG_PLATAFORMA://2 {"OPCION":2, "ID":123}
		    $ipaddress = "";
            if ($_SERVER['REMOTE_ADDR'])
                $ipaddress = $_SERVER['REMOTE_ADDR'];
			$chipID = $data->ID;
			echo $db->registrar_plataforma($chipID,$ipaddress);//registro o actualizo la plataforma en el servidor	
		break;
		case REG_RUTA://3 {"OPCION":3}
			echo "RUTA REGISTRADA"; 
		break;
		case ENVIAR_RUTA://1 {"OPCION":1,"USER":1,"ID":11569620,"DESDE":1, "HASTA":2}
			$ruta = $db->obtener_ruta($data->DESDE,$data->HASTA);
			//obtengo la ruta existente entre dos puntos
			echo $db->registrar_peticion($data->USER,$data->ID,"ENVIO",$ruta,$data->HASTA);
		break;
		case VERIFICAR_MENSAJE://11 {"OPCION":11, "ID":11569620}
			echo $db->obtener_peticion($data->ID); 
		break;
		default: "CODIGO ERRONEO"; break;
	}
}
?>