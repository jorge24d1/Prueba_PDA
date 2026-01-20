import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiService {
  // Cambiado: URL de producción en Azure
  // static const String baseUrl = 'https://nextgen-motors2-bsbudccxcnadgabd.canadacentral-01.azurewebsites.net/api/usuario';
  static const String baseUrl =
      'http://192.168.1.4:8080/api/usuario'; // Localhost IPv4

  Future<Map<String, dynamic>> login(String correo, String password) async {
    final response = await http.post(
      Uri.parse('$baseUrl/login'),
      body: {'correo': correo, 'password': password},
    );

    if (response.statusCode == 200) {
      final data = json.decode(response.body);
      if (data['success'] == true) {
        // Guardar userId para uso futuro
        final prefs = await SharedPreferences.getInstance();
        await prefs.setString('userId', data['userId']);
        await prefs.setString('nombre', data['nombre']);
      }
      return data;
    } else {
      throw Exception('Error en login: ${response.statusCode}');
    }
  }

  Future<List<dynamic>> getCitas() async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getString('userId');

    if (userId == null) throw Exception('Usuario no logueado');

    final response = await http.get(Uri.parse('$baseUrl/$userId/citas'));

    if (response.statusCode == 200) {
      return json.decode(response.body);
    } else {
      throw Exception('Error al cargar citas');
    }
  }

  Future<void> sendFcmToken(String token) async {
    final prefs = await SharedPreferences.getInstance();
    final userId = prefs.getString('userId');

    if (userId != null) {
      print('📤 Enviando token al servidor para usuario $userId...');
      try {
        final response = await http.post(
          Uri.parse('$baseUrl/$userId/fcm-token'),
          body: {'token': token},
        );
        print(
          '📥 Respuesta del servidor al guardar token: ${response.statusCode}',
        );
        if (response.statusCode != 200) {
          print('❌ Error cuerpo respuesta: ${response.body}');
        }
      } catch (e) {
        print('❌ Error enviando token al backend: $e');
      }
      print('📱 Token FCM enviado al servidor (intento finalizado)');
    } else {
      print('⚠️ No se envió el token porque no hay usuario logueado.');
    }
  }

  // Nuevo método para Chatbot Gemini
  Future<Map<String, dynamic>> obtenerRecomendacion(String mensaje) async {
    final response = await http.post(
      Uri.parse('$baseUrl/recomendacion'),
      headers: {'Content-Type': 'application/json'},
      body: json.encode({'mensaje': mensaje}),
    );

    if (response.statusCode == 200) {
      // Decodificamos utf8 para caracteres especiales en español
      return json.decode(utf8.decode(response.bodyBytes));
    } else {
      throw Exception('Error al conectar con Dante AI');
    }
  }
}
