import argparse
import subprocess
import sys
from pathlib import Path

def ffmpeg_available():
    try:
        subprocess.run(["ffmpeg", "-version"], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
        return True
    except Exception:
        return False

def process_dir(dir_path: Path, dry_run: bool):
    if not dir_path.exists():
        print(f"Directory not found: {dir_path}")
        return 1
    targets = list(dir_path.glob("phonk*.ogg"))
    rc = 0
    for src in targets:
        if src.name.lower() == "phonk6.ogg":
            print(f"Skipping {src}")
            continue
        tmp = src.with_suffix(".tmp.ogg")
        cmd = [
            "ffmpeg",
            "-y",
            "-i",
            str(src),
            "-filter:a",
            "volume=-10dB",
            str(tmp)
        ]
        print(" ".join(cmd))
        if not dry_run:
            try:
                subprocess.run(cmd, check=True)
                src.unlink()
                tmp.rename(src)
            except subprocess.CalledProcessError as e:
                print(f"ffmpeg failed for {src}: {e}")
                if tmp.exists():
                    tmp.unlink()
                rc = 2
    return rc

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dir", default=str(Path("src/main/resources/assets/phonkedit/sounds/phonk")))
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()
    if not ffmpeg_available():
        print("ffmpeg not found in PATH")
        return 127
    return process_dir(Path(args.dir), args.dry_run)

if __name__ == "__main__":
    sys.exit(main())
