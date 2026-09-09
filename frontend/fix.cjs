const fs = require('fs');
const path = require('path');

function walk(dir) {
    let results = [];
    const list = fs.readdirSync(dir);
    list.forEach(file => {
        file = path.join(dir, file);
        const stat = fs.statSync(file);
        if (stat && stat.isDirectory()) {
            results = results.concat(walk(file));
        } else if (file.endsWith('.js') || file.endsWith('.jsx')) {
            results.push(file);
        }
    });
    return results;
}

const files = walk('e:/HRMS/frontend/src');
files.forEach(file => {
    let content = fs.readFileSync(file, 'utf8');
    content = content.replace(/`\\\/api\//g, "`\${import.meta.env.VITE_API_URL}/api/");
    fs.writeFileSync(file, content);
});
console.log('Fixed template literals in ' + files.length + ' files');
