<?php
require_once "config.php";
//require_once "funciones.php";
require_once "conexion.php";

$db = BaseDatos::getInstancia();
if(file_get_contents("php://input")){
	$data = json_decode(file_get_contents("php://input"));
	switch($data->OPCION){
		case SECTORES://9 {"OPCION":9, ID:11569620}//
			echo $db->getSector($data->ID);//obtengo el listado de sectores a seleccionar por el usuario	
	    break;
		case VERIFICAR_CONEXION://7 {"OPCION":7}//
			echo $db->verificar_conexion();//verificar la conexion con el servidor
	    break;
		case CONECTAR://0 {"OPCION":0}//
			echo $db->login();//codigo para verificar usuario y contraseña
		break;
		case REG_USUARIO://4 {"OPCION":4}
			echo $db->registrar_usuario(); //codigo para registrar usuario y contraseña
		break;
		case OBTENER_PLATAFORMA://10 {"OPCION":10,"USER":0}//
			echo $db->obtener_plataforma($data->USER);//obtengo el listado de plataformas a seleccionar por el usuario		
		break;
		case REG_PLATAFORMA://2 {"OPCION":2, "ID":123, "MSJ":"192.168.0.128"}//
			echo $db->registrar_plataforma($data->ID,$data->MSJ);//registro o actualizo la plataforma en el servidor	
		break;
		case REG_RUTA://3 {"OPCION":3}
			echo "RUTA REGISTRADA"; 
		break;
		case ENVIAR_RUTA://1 {"OPCION":1,"USER":1,"ID":11569620,"DESDE":1, "HASTA":2}
			$ruta = $db->obtener_ruta($data->DESDE,$data->HASTA);
			//obtengo la ruta existente entre dos puntos
			echo $db->registrar_peticion($data->USER,$data->ID,"ENVIO",$ruta,$data->HASTA);
		break;
		case OBTENER_PETICION://12 {"OPCION":12, "ID":11569620}
			echo $db->getPeticion($data->ID);
		break;
		case REGISTRAR_TOKEN://14 {"OPCION":14, "TOKEN":"AAAxx"}//
			echo $db->registrarToken($data->TOKEN,$data->USER);
		break;
		case ENVIAR_NOTIFICACION://13
			echo $db->enviar_notificacion($data->ID,$data->MSJ);
		break;
		case ASOCIAR_PLATAFORMA://15
		    echo $db->ascociarPlataforma($data->USER,$data->ID,$data->NOMBRE);
		break;  
		case ASOCIAR_SECTOR://
		    echo $db->asociarSector($data->ID,$data->NOMBRE,$data->MAC);
		break;
		case ACTUALIZAR_SECTOR_ACTUAL://17
		    echo $db->registrarSectorActual($data->ID,$data->ACTUAL);
		break;
		case OCUPAR_PLATAFORMA: //6
		    echo $db->ocuparPlataforma($data->ID);
		break;
		case LIBERAR_PLATAFORMA: //18
		    //$db->enviar_notificacion($data->ID,$data->MSJ);
		    echo $db->liberarPlataforma($data->ID,$data->MSJ);		    
		break;
		case LIBERAR: //21
		    //$db->enviar_notificacion($data->ID,$data->MSJ);
		    echo $db->liberar($data->ID);		    
		break;
		default: "CODIGO ERRONEO"; break;
	}
}
?>