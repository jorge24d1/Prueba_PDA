import 'package:flutter/material.dart';
import 'package:mobile_app/services/api_service.dart';
import 'package:mobile_app/screens/chat_screen.dart';
import 'package:shared_preferences/shared_preferences.dart';

class HomeScreen extends StatefulWidget {
  @override
  _HomeScreenState createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final _apiService = ApiService();
  List<dynamic> _citas = [];
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _cargarCitas();
  }

  Future<void> _cargarCitas() async {
    try {
      final citas = await _apiService.getCitas();
      setState(() {
        _citas = citas;
        _isLoading = false;
      });
    } catch (e) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(SnackBar(content: Text('Error cargando citas')));
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Mis Citas'),
        actions: [
          IconButton(
            icon: Icon(Icons.exit_to_app),
            onPressed: () async {
              // Cerrar sesión
              final prefs = await SharedPreferences.getInstance();
              await prefs.clear(); // Borra userId y nombre

              Navigator.pushNamedAndRemoveUntil(
                context,
                '/login',
                (route) => false,
              );
            },
            tooltip: 'Cerrar Sesión',
          ),
        ],
      ),
      body: _isLoading
          ? Center(child: CircularProgressIndicator())
          : RefreshIndicator(
              onRefresh: _cargarCitas,
              child: ListView.builder(
                itemCount: _citas.length,
                itemBuilder: (context, index) {
                  final cita = _citas[index];
                  return Card(
                    margin: EdgeInsets.all(8.0),
                    child: ListTile(
                      leading: Icon(Icons.calendar_today, color: Colors.blue),
                      title: Text(cita['vehiculo'] ?? 'Cita General'),
                      subtitle: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text('Estado: ${cita['estado']}'),
                          Text(
                            'Fecha: ${cita['fechaAsignada'] ?? "Pendiente asignación"}',
                          ),
                        ],
                      ),
                      trailing: Icon(Icons.info_outline, color: Colors.blue),
                      onTap: () {
                        showDialog(
                          context: context,
                          builder: (context) => AlertDialog(
                            title: Text('Detalles de la Cita'),
                            content: Column(
                              mainAxisSize: MainAxisSize.min,
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  "Vehículo:",
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                Text(cita['vehiculo'] ?? 'No especificado'),
                                SizedBox(height: 8),
                                Text(
                                  "Fecha Asignada:",
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                Text(
                                  cita['fechaAsignada'] ??
                                      "Pendiente de asignación",
                                ),
                                SizedBox(height: 8),
                                Text(
                                  "Notas del Asesor:",
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                                Container(
                                  width: double.infinity,
                                  padding: EdgeInsets.all(8),
                                  decoration: BoxDecoration(
                                    color: Colors.grey[100],
                                    borderRadius: BorderRadius.circular(8),
                                  ),
                                  child: Text(
                                    cita['notasAdmin'] != null &&
                                            cita['notasAdmin']
                                                .toString()
                                                .isNotEmpty
                                        ? cita['notasAdmin']
                                        : "Sin notas adicionales.",
                                    style: TextStyle(
                                      fontStyle: FontStyle.italic,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            actions: [
                              TextButton(
                                onPressed: () => Navigator.pop(context),
                                child: Text('Cerrar'),
                              ),
                            ],
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
            ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => ChatScreen()),
          );
        },
        label: Text('Chat con Dante'),
        icon: Icon(Icons.chat),
        backgroundColor: Colors.blueAccent,
      ),
    );
  }
}
