import sys
import re
import os
import zlib
import base64
import urllib.request
import subprocess

def encode_kroki(text):
    compressed = zlib.compress(text.encode('utf-8'))
    return base64.urlsafe_b64encode(compressed).decode('ascii')

def download_diagram(diagram_type, text, output_filename):
    encoded = encode_kroki(text)
    url = f"https://kroki.io/{diagram_type}/png/{encoded}"
    try:
        req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
        with urllib.request.urlopen(req) as response, open(output_filename, 'wb') as out_file:
            data = response.read()
            out_file.write(data)
        return True
    except Exception as e:
        print(f"Error downloading {diagram_type} diagram: {e}")
        return False

def convert_file(input_file, output_file=None):
    if not os.path.exists(input_file):
        print(f"File not found: {input_file}")
        return
        
    if not output_file:
        output_file = os.path.splitext(input_file)[0] + ".docx"

    print(f"Processing {input_file}...")
    with open(input_file, 'r', encoding='utf-8') as f:
        content = f.read()

    # Process mermaid blocks
    mermaid_pattern = re.compile(r'```mermaid\n(.*?)\n```', re.DOTALL | re.IGNORECASE)
    mermaid_blocks = mermaid_pattern.findall(content)
    
    for i, m_code in enumerate(mermaid_blocks):
        png_file = f'temp_mermaid_{i}.png'
        print(f"  Rendering Mermaid diagram {i+1}/{len(mermaid_blocks)}...")
        if download_diagram('mermaid', m_code, png_file):
            original_block = f'```mermaid\n{m_code}\n```'
            replacement = f'![Mermaid Diagram]({png_file})'
            # Note: content.replace is exact case sensitive, need to replace using re.sub
            # to handle case insensitivity of 'mermaid' in the regex, we just use regex sub
            pattern = re.compile(r'```mermaid\n' + re.escape(m_code) + r'\n```', re.IGNORECASE | re.DOTALL)
            content = pattern.sub(replacement, content, count=1)

    # Process plantuml blocks
    plantuml_pattern = re.compile(r'```(?:plantuml|puml)\n(.*?)\n```', re.DOTALL | re.IGNORECASE)
    plantuml_blocks = plantuml_pattern.findall(content)
    
    for i, p_code in enumerate(plantuml_blocks):
        png_file = f'temp_plantuml_{i}.png'
        print(f"  Rendering PlantUML diagram {i+1}/{len(plantuml_blocks)}...")
        
        # Ensure plantuml block has @startuml and @enduml
        kroki_code = p_code
        if "@startuml" not in kroki_code:
            kroki_code = "@startuml\n" + kroki_code + "\n@enduml"
            
        if download_diagram('plantuml', kroki_code, png_file):
            # Using sub to safely replace
            pattern = re.compile(r'```(?:plantuml|puml)\n' + re.escape(p_code) + r'\n```', re.IGNORECASE | re.DOTALL)
            replacement = f'![PlantUML Diagram]({png_file})'
            content = pattern.sub(replacement, content, count=1)

    # Save temporary markdown
    temp_md = "temp_pandoc_input.md"
    with open(temp_md, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"  Converting to {output_file} using Pandoc...")
    try:
        # Use pandoc to convert the modified markdown to docx
        subprocess.run(['pandoc', temp_md, '-o', output_file], check=True)
        print(f"  Success: {output_file}")
    except subprocess.CalledProcessError as e:
        print(f"  Failed to convert using Pandoc: {e}")

    # Cleanup temp files
    if os.path.exists(temp_md):
        os.remove(temp_md)
    for i in range(len(mermaid_blocks)):
        if os.path.exists(f'temp_mermaid_{i}.png'):
            os.remove(f'temp_mermaid_{i}.png')
    for i in range(len(plantuml_blocks)):
        if os.path.exists(f'temp_plantuml_{i}.png'):
            os.remove(f'temp_plantuml_{i}.png')

if __name__ == "__main__":
    if len(sys.argv) > 1:
        input_f = sys.argv[1]
        output_f = sys.argv[2] if len(sys.argv) > 2 else None
        convert_file(input_f, output_f)
    else:
        # Batch convert files in docs directory
        docs_dir = "docs"
        if os.path.exists(docs_dir):
            import glob
            for root, dirs, files in os.walk(docs_dir):
                for file in files:
                    if file.endswith(".md"):
                        convert_file(os.path.join(root, file))
        else:
            print("Usage: python export_to_docx.py <input.md> [output.docx]")
