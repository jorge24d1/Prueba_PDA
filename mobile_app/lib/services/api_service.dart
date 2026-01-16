import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';

class ApiService {
  // Cambiado: URL de producción en Azure
  static const String baseUrl =
      'https://nextgen-motors2-bsbudccxcnadgabd.canadacentral-01.azurewebsites.net/api/usuario';

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
      await http.post(
        Uri.parse('$baseUrl/$userId/fcm-token'),
        body: {'token': token},
      );
      print('📱 Token FCM enviado al servidor');
    }
  }
}
