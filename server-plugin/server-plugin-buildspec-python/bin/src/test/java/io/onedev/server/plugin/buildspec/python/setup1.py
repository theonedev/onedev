import os
from setuptools import setup, find_packages

with open(os.path.join(os.path.dirname(__file__), "README.md")) as readme:
    README = readme.read()

# allow setup.py to be run from any path
os.chdir(os.path.normpath(os.path.join(os.path.abspath(__file__), os.pardir)))

setup(
    name="wooey",
    version="0.13.3",
    packages=find_packages(),
    scripts=["scripts/wooify"],
    entry_points={
        "console_scripts": [
            "wooify = wooey.backend.command_line:bootstrap",
        ]
    },
    python_requires=">3.5.0",
    install_requires=[
        "celery>=4,<6",
        "clinto>=0.5.1",
        "Django>=3,<5",
        "django-autoslug",
        "django-storages",
        'eventlet>=0.22.1 ;platform_system=="Windows"',
        'importlib-metadata<5.0 ;python_version<="3.7"',
        'pypiwin32 ;(platform_system=="Windows" and python_version>"3.4")',
    ],
    extras_require={
        "dev": [
            "boto3",
            "coverage",
            "factory-boy < 3.0.0",
            "mock",
            "pytest",
            "pytest-cov",
            "sphinx",
            "watchdog[watchmedo]",
        ]
    },
    include_package_data=True,
    description="A Django app which creates a web GUI and task interface for argparse scripts",
    url="http://www.github.com/wooey/wooey",
    author="Chris Mitchell <chris.mit7@gmail.com>, Martin Fitzpatrick <martin.fitzpatrick@gmail.com>",
    classifiers=[
        "Environment :: Web Environment",
        "Framework :: Django",
        "Intended Audience :: Developers",
        "License :: OSI Approved :: BSD License",
        "Operating System :: OS Independent",
        "Programming Language :: Python",
        "Programming Language :: Python :: 3",
        "Programming Language :: Python :: 3.7",
        "Programming Language :: Python :: 3.8",
        "Programming Language :: Python :: 3.9",
        "Programming Language :: Python :: 3.10",
        "Topic :: Internet :: WWW/HTTP",
        "Topic :: Internet :: WWW/HTTP :: Dynamic Content",
    ],
)
