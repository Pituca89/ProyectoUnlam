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
    /**APK */
    public function obtener_plataforma($user){
        $query = "call obtenerPlataforma(".$user.");";
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
        $query = "CALL registrarPeticion(".$iduser.",".$idpl.",'".$codigo."','".$dato."');";
        if (!$this->conexion->multi_query($query)) {
            echo "Falló CALL: (" . $this->conexion->errno . ") " . $this->conexion->error;
        }  
        $response = array();
        $response['opcion'] = 'PETICION';  
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
        $response['opcion'] = "OK";
        $response['actual'] = $destino;
        return json_encode($response);
    }
}
?>