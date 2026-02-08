document.addEventListener('DOMContentLoaded', () => {
    const viewer = document.getElementById('car-viewer');
    const colorButtons = document.querySelectorAll('.color-btn');
    const colorNameDisplay = document.getElementById('selected-color-name');

    // Helper to convert Hex to RGBA array [r, g, b, a]
    function hexToRgba(hex) {
        let c;
        if (/^#([A-Fa-f0-9]{3}){1,2}$/.test(hex)) {
            c = hex.substring(1).split('');
            if (c.length === 3) {
                c = [c[0], c[0], c[1], c[1], c[2], c[2]];
            }
            c = '0x' + c.join('');
            return [
                ((c >> 16) & 255) / 255,
                ((c >> 8) & 255) / 255,
                (c & 255) / 255,
                1.0
            ];
        }
        return [1, 1, 1, 1]; // Default white
    }
    
    // Function to update color name
    function updateColorName(name, colorHex) {
        if (colorNameDisplay) {
            colorNameDisplay.textContent = `Mazda 3 ${name}`;
            // Optional: style text color to match or be neutral
            // colorNameDisplay.style.color = colorHex === '#ffffff' ? '#333' : colorHex; 
        }
    }
    
    // Set initial active state
    if(colorButtons.length > 0) {
        // Select red by default or first one
        const defaultBtn = colorButtons[0];
        defaultBtn.classList.add('active');
        if(colorNameDisplay) updateColorName(defaultBtn.getAttribute('data-name'), defaultBtn.getAttribute('data-color'));
    }

    if (viewer) {
        viewer.addEventListener('load', () => {
            console.log('Modelo 3D cargado correctamente');
        });

        colorButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                // Remove active class from all
                colorButtons.forEach(b => b.classList.remove('active'));
                // Add to clicked
                btn.classList.add('active');
                
                const colorHex = btn.getAttribute('data-color');
                const colorName = btn.getAttribute('data-name');
                const colorRgba = hexToRgba(colorHex);
                
                updateColorName(colorName, colorHex);

                if (!viewer.model) return;

                const excludeKeywords = ['glass', 'window', 'windshield', 'tire', 'rubber', 'wheel', 'rim', 'chrome', 'chromium', 'light', 'lamp', 'interior', 'seat', 'dash', 'plastic_black', 'grille'];
                
                let matsFound = 0;

                viewer.model.materials.forEach(material => {
                    const matName = material.name.toLowerCase();
                    const isExcluded = excludeKeywords.some(keyword => matName.includes(keyword));

                    if (!isExcluded) {
                        material.pbrMetallicRoughness.setBaseColorFactor(colorRgba);
                        console.log(`Color aplicado a material: ${material.name}`);
                        matsFound++;
                    }
                });

                if (matsFound === 0) {
                    console.warn('No se encontraron materiales aptos para pintar.');
                }
            });
        });
    }
});
