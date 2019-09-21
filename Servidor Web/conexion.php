<?php
require("GrafoDijkstra.php");
class BaseDatos
{
    protected $conexion;
    private $_host = "localhost";
    private $_user = "u511241329_bepim";
    private $_pass = "proyectounlam2019";
    private $_db   = "u511241329_bepim";
    private $query = "";
    // Almacenar una unica instancia
    private static $_instancia;
    private $result;
    public function __construct(){
        $this->conexion = mysqli_connect($this->_host,$this->_user,$this->_pass,$this->_db);
        // Manejar error en base de datos
        if(!$this->conexion) die("Connection failed: ".mysqli_connect_error());
    }
    // ================================================
    // Metodo para obtener instancia de base de datos
    // ================================================
    public static function getInstancia(){
        if(!isset(self::$_instancia)){
            self::$_instancia = new self;
        }
        return self::$_instancia;
    }
    // Metodo vacio __close para evitar duplicacion
    private function __close(){}
        // Metodo para obtener la conexion a la base de datos
    public function getConnection(){
        return $this->conexion;
    }

    public function desconectar()
    {
        if ($this->conexion) {
            mysqli_close($this->$conexion);
        }
    }

    /**APK */
    public function getSector($id){
        $query = "call getSector(".$id.");";
        if(!$result = mysqli_query($this->conexion,$query)) die();
    
        $response = array();
        $response['opcion'] = 'SECTORES';
        $miarray = array();
        $i = 0;
        while($row = $result->fetch_object()){
            $miarray[$i] = $row;
            $i++;
        }
        $response['sectores'] = $miarray;
        return json_encode($response);
    }

    public function getPeticion($id){
        $query = "call getPeticion(".$id.");";
        if(!$result = mysqli_query($this->conexion,$query)) die();
    
        $response = array();
        if($row = $result->fetch_object()){
            $response['codigo'] = $row->cod;
            $response['dato'] = $row->dato;
        }
        return json_encode($response);
    }
    /**APK */
    public function verificar_conexion(){
        /**Podria agregar una prueba de conexion a la base de datos */
        $json = array(
            'opcion' => 'CONECTADO',//Enviar la ruta de prueba almacenada en la base de datos
        );
        $jsonEncode = json_encode($json);
        return $jsonEncode;
    }
    /**APK */
    public function login(){
        /**Validar usuario y contraseña */
        $json = array(
            'opcion' => 'USUARIO CONECTADO',//Enviar la ruta de prueba almacenada en la base de datos
        );
        $jsonEncode = json_encode($json);
        return $jsonEncode; 
    }
    /**APK */
    public function registrar_usuario(){
        /**Registrar usuario y contraseña */
        $json = array(
            'opcion' => 'USUARIO REGISTRADO',//Enviar la ruta de prueba almacenada en la base de datos
        );
        $jsonEncode = json_encode($json);
        
        return $jsonEncode; 
    }
    
    public function registrar_plataforma($id,$ip){
        $query = "call registrarPlataforma('".$id."','".$ip."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        else {
            $response['codigo'] = 'REGISTRO';
            $response['dato'] = 'OK';
            return json_encode($response);
        }
    }
    public function registrarToken($token,$user){
        $query = "call registrarToken('".$token."','".$user."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        else {
            $response['opcion'] = 'TOKEN';
            $response['dato'] = 'OK';
            return json_encode($response);
        }
    }
    
    public function ascociarPlataforma($user,$id,$nombre){
        $query = "call asociarUsuarioPlataforma('".$user."','".$id."','".$nombre."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        
        else {
            if($row = $result->fetch_object()){
                $response['opcion'] = $row->SALIDA;
            }
            return json_encode($response);
        }
    }
    public function asociarSector($id,$nombre,$mac){
        $query = "call registrarSector('".$nombre."','".$id."','".$mac."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        
        else {
            if($row = $result->fetch_object()){
                $response['opcion'] = $row->SALIDA;
            }
            return json_encode($response);
        }
    }
    /**APK */
    public function obtener_plataforma($user){
        $query = "call obtenerPlataforma('".$user."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
    
        $response = array();
        $response['opcion'] = 'PLATAFORMA';
        $miarray = array();
        $i = 0;
        while($row = $result->fetch_object()){
            $miarray[$i] = $row;
            $i++;
        }
        $response['plataforma'] = $miarray;
        return json_encode($response);
    }
    /**APK */
    public function obtener_ruta($iddesde,$idhasta){
        $query = "call obtenerRutas(".$iddesde.",".$idhasta.");";

        if (!$this->conexion->multi_query($query)) {
            echo "Falló CALL: (" . $this->conexion->errno . ") " . $this->conexion->error;
        }
        $grafo = array();
        $camino = array();
        $i = 0;        
        do {
            if ($resultado = $this->conexion->store_result()) {
                while($row = $resultado->fetch_object()){
                    $grafo[$i] = array($row->desde,$row->hasta,1);
                    $key = $row->desde."-".$row->hasta;
                    $temp = array($key => $row->ruta);    
                    array_push($camino,$temp);               
                    $i++;
                }               
                $resultado->free();
            } else {
                if ($this->conexion->errno) {
                    die();
                }
            }
        } while ($this->conexion->more_results() && $this->conexion->next_result());
        $ruta = dijkstra($grafo,$iddesde,$idhasta);
        $rutaFin = "-";
        for($i = 0; $i < count($ruta); $i++){
            $pri = $i;
            $ult = $i + 1;
            if($pri < count($ruta) - 1){
                $key = $ruta[$pri]."-".$ruta[$ult];
                foreach($camino as $c){
                    if(isset($c[$key])){
                        $rutaFin .= $c[$key]."|";
                    }
                }
            }
        }
        return substr($rutaFin,0,strlen($rutaFin)-1).'#';      
    }
    /**APK */
    public function registrar_peticion($iduser,$idpl,$codigo,$dato,$destino){
        $query = "CALL registrarPeticion(".$iduser.",".$idpl.",'".$codigo."','".$dato."',".$destino.");";
        if (!$this->conexion->multi_query($query)) {
            echo "Falló CALL: (" . $this->conexion->errno . ") " . $this->conexion->error;
        }  
        $response = array();
        $response['opcion'] = 'PETICION';  
        do {
            if ($resultado = $this->conexion->store_result()) { 
                if($row = $resultado->fetch_object()){   
                    if($row->SALIDA == "OCUPADO"){
                        $response['opcion'] = "OCUPADO";
                    }
                    else{
                        $response['opcion'] = "OK";        
                    }
                }
                $resultado->free();
            } else {
                if ($this->conexion->errno) {
                    $response['opcion'] = "ERROR";
                    return json_encode($response);
                }
            }
        } while ($this->conexion->more_results() && $this->conexion->next_result());
        $response['actual'] = $destino;
        return json_encode($response);
    }
    
    public function registrarSectorActual($chipid,$idsector){
        $query = "CALL actualizarSectorTraining('".$chipid."',".$idsector.");";
        if (!$this->conexion->multi_query($query)) {
            echo "Falló CALL: (" . $this->conexion->errno . ") " . $this->conexion->error;
        }  
        $response = array();
        $response['opcion'] = 'ACTUAL';  
        do {
            if ($resultado = $this->conexion->store_result()) {            
                $resultado->free();
            } else {
                if ($this->conexion->errno) {
                    $response['opcion'] = "ERROR";
                    return json_encode($response);
                }
            }
        } while ($this->conexion->more_results() && $this->conexion->next_result());
        $response['actual'] = $destino;
        return json_encode($response);
    }
    public function enviar_notificacion($id,$msj){

        $path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
        $server_key='AAAAN6C7Vaw:APA91bHpmvKHGOwhqA1_qYbiJpNEd7VGZ4bt84DLNYfQSBSDy1fKpMha6AUQfSyHYx7bunpBVNHSqdlkWLRtgfiGXg8wYkJFXhxnYpS6q38pDSmKFbF3PaQyfFSOTcaZCMyvwEzCxVxU';
        $query = "call getToken('".$id."')";
         // Obtención del Token        
        if (!$this->conexion->multi_query($query)) {
            echo "Falló CALL: (" . $this->conexion->errno . ") " . $this->conexion->error;
        }
         do {
            if ($resultado = $this->conexion->store_result()) {
                while($row = mysqli_fetch_row($resultado)){
                    $keyToken = $row[5];
                    $ip = $row[1];
                    $disponible = $row[2];
                    $sector_actual = $row[3];
                    $nombre = $row[4];
                    $fields = array(
                        'to'=>'/topics/'.$id,//$keyToken, 
                        'notification'=>array(
                            'title'=>"BePIM",
                            'body'=>$msj
                        ),
                        'data'=>array(
                            'titulo'=>"BePIM",
                            'msj'=> $msj,
                            'chipid'=> $id,
                            'nombre'=> $nombre,
                            'ip'=> $ip,
                            'sectoractual'=> $sector_actual,
                            'disponible'=> $disponible
                        )
                    );
                    $headers = array( 
                        'Authorization:key=' .$server_key,
                        'Content-Type:application/json'
                    );
                    $ch = curl_init($path_to_fcm);
                    curl_setopt($ch, CURLOPT_POST, true);
                    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
                    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
                    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
                    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
                    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
                    
                    $resultado = curl_exec($ch);
                    if ($resultado == FALSE)
                        die('Curl failed: ' . curl_error($ch));
                    curl_close($ch);                
                }               
                $resultado->free();
                $response = array();
                $response['codigo'] = 'MENSAJE';
                $response['dato'] = 'OK';
                echo json_encode($response);
            } else {
                if ($this->conexion->errno) {
                    die();
                }
            }
        } while ($this->conexion->more_results() && $this->conexion->next_result());
    }
    
    public function ocuparPlataforma($id){
        $query = "call ocuparPlataforma('".$id."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        
        else {
            if($row = $result->fetch_object()){
                $response['opcion'] = $row->SALIDA;
            }
            return json_encode($response);
        }
    }
    public function liberar($id){
        $query = "call liberar('".$id."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        
        else {
            if($row = $result->fetch_object()){
                $response['opcion'] = $row->SALIDA;
            }
            return json_encode($response);
        }
    }
    public function liberarPlataforma($id,$msj){
        
        $query = "call liberarPlataforma('".$id."');";
        if(!$result = mysqli_query($this->conexion,$query)) die();
        /*
        else {
            if($row = $result->fetch_object()){
                $response['opcion'] = "ACTUAL";
                $response['actual'] = $row->SALIDA;
            }
            
        }
        */
        //return $this->enviar_notificacion($id,$msj);
        $path_to_fcm = 'https://fcm.googleapis.com/fcm/send';
        $server_key='AAAAN6C7Vaw:APA91bHpmvKHGOwhqA1_qYbiJpNEd7VGZ4bt84DLNYfQSBSDy1fKpMha6AUQfSyHYx7bunpBVNHSqdlkWLRtgfiGXg8wYkJFXhxnYpS6q38pDSmKFbF3PaQyfFSOTcaZCMyvwEzCxVxU';
        $keyToken = $row[5];
        $ip = $row[1];
        $disponible = $row[2];
        $sector_actual = $row[3];
        $nombre = $row[4];
        $fields = array(
            'to'=>'/topics/'.$id,//$keyToken, 
            'notification'=>array(
                'title'=>"BePIM",
                'body'=>$msj
            ),
            'data'=>array(
                'titulo'=>"BePIM",
                'msj'=> $msj,
                'chipid'=> $id,
                'nombre'=> $nombre,
                'ip'=> $ip,
                'sectoractual'=> $sector_actual,
                'disponible'=> $disponible
            )
        );
        $headers = array( 
            'Authorization:key=' .$server_key,
            'Content-Type:application/json'
        );
        $ch = curl_init($path_to_fcm);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($fields));
        
        $resultado = curl_exec($ch);
        if ($resultado == FALSE)
            die('Curl failed: ' . curl_error($ch));
        curl_close($ch);      
    }
    
}
?>