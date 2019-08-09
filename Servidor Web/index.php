<?php
require_once "config.php";
require_once "funciones.php";
require_once "conexion.php";

$db = BaseDatos::getInstancia();
if(file_get_contents("php://input")){
    echo 1;
	$data = json_decode(file_get_contents("php://input"));
	switch($data->OPCION){
		case SECTORES:
			echo $db->getSector();
			$db->desconectar();		
	    break;
		case RUTA_PRUEBA:
			$db->enviar_ruta_prueba($data->ID);
			$db->desconectar();	
	    break;
		case VERIFICAR_CONEXION:
			echo $db->verificar_conexion();
			$db->desconectar();
	    break;
		case CONECTAR: 
			echo $db->login();
			$db->desconectar();
		break;
		case REG_USUARIO:
			echo $db->registrar_usuario();
			$db->desconectar();
		break;
		case OBTENER_PLATAFORMA:
			echo $db->obtener_ip_plataforma($data->ID);
			$db->desconectar();		
		break;
		case REG_PLATAFORMA: 
		    $ipaddress = "";
            if ($_SERVER['REMOTE_ADDR'])
                $ipaddress = $_SERVER['REMOTE_ADDR'];
			$chipID = $data->ID;
			echo $db->registrar_plataforma($chipID,$ipaddress);
			$db->desconectar();		
		break;
		case REG_RUTA: 
			echo "RUTA REGISTRADA"; 
		break;
		case ENVIAR_RUTA: 
			echo "RUTA ENVIADA"; 
		break;
		case PLATAFORMA_DISPONIBLE: 
			echo "PLATAFORMA DISPONIBLE"; 
		break;
		case OCUPAR_PLATAFORMA: 
			echo "OCUPAR PLATAFORMA"; 
		break;
		default: "CODIGO ERRONEO"; break;
	}
}else{
}

////curl_close($ch);
?>